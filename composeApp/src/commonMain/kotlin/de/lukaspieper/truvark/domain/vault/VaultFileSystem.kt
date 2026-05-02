/*
 * SPDX-FileCopyrightText: 2023 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.domain.vault

import de.lukaspieper.truvark.data.io.DirectoryInfo
import de.lukaspieper.truvark.data.io.FileInfo
import de.lukaspieper.truvark.data.io.FileSystem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import java.io.FileInputStream
import java.io.OutputStream
import java.util.concurrent.ConcurrentHashMap
import kotlin.uuid.Uuid

/**
 * A "fileSystem" that provies special-purpose functions for common vault operations and speeds up access through
 * caching.
 */
public class VaultFileSystem private constructor(
    private val fileSystem: FileSystem,
    private val rootDirectory: DirectoryInfo,
    private val cipherFilesRootDirectory: DirectoryInfo,
    public val vaultFile: FileInfo,
    public val folderIndexFile: FileInfo,
    /** Map caching the directories inside [cipherFilesRootDirectory]. */
    private val cipherDirectoryInfoCache: ConcurrentHashMap<Uuid, DirectoryInfo>
) {
    public val decryptionRootDirectory: DirectoryInfo
        get() = runBlocking { fileSystem.findOrCreateDirectory(rootDirectory, DECRYPTION_DIRECTORY_NAME) }

    public suspend fun findFileIndexFileOrNull(folderId: Uuid): FileInfo? {
        return fileSystem.findFileOrNull(cipherFilesRootDirectory, "${folderId.toHexString()}.index")
    }

    public suspend fun findOrCreateFileIndexFile(folderId: Uuid): FileInfo {
        return fileSystem.findOrCreateFile(cipherFilesRootDirectory, "${folderId.toHexString()}.index")
    }

    public fun listFilesInCipherDirectory(name: Uuid): Flow<FileInfo> {
        return cipherDirectoryInfoCache[name]?.let { directoryInfo ->
            fileSystem.listFiles(directoryInfo).filter { Uuid.parseHexOrNull(it.fullName) != null }
        } ?: emptyFlow()
    }

    public suspend fun findFileInCipherDirectory(directoryName: Uuid, fileName: Uuid): FileInfo? {
        return cipherDirectoryInfoCache[directoryName]?.let { directoryInfo ->
            fileSystem.findFileOrNull(directoryInfo, fileName.toHexString())
        }
    }

    public suspend fun createFileInCipherDirectory(directoryName: Uuid, fileName: Uuid): FileInfo {
        val cipherDirectory = cipherDirectoryInfoCache.computeIfAbsent(directoryName) {
            runBlocking { fileSystem.findOrCreateDirectory(cipherFilesRootDirectory, directoryName.toHexString()) }
        }

        return fileSystem.createFile(cipherDirectory, fileName.toHexString())
    }

    public suspend fun deleteFileFromCipherDirectory(directoryName: Uuid, fileName: Uuid) {
        cipherDirectoryInfoCache[directoryName]?.let { cipherDirectory ->
            fileSystem.findFileOrNull(cipherDirectory, fileName.toHexString())?.let { file ->
                fileSystem.delete(file)
            }
        }
    }

    public fun deleteCipherDirectory(name: Uuid) {
        cipherDirectoryInfoCache[name]?.let { cipherDirectory ->
            fileSystem.delete(cipherDirectory)
        }

        cipherDirectoryInfoCache.remove(name)
    }

    public suspend fun relocateFileIntoCipherDirectory(
        sourceFileName: Uuid,
        sourceParentDirectoryName: Uuid,
        targetDirectoryName: Uuid
    ) {
        val sourceParentDirectory = cipherDirectoryInfoCache[sourceParentDirectoryName]!!
        val sourceFile = findFileInCipherDirectory(sourceParentDirectoryName, sourceFileName)!!

        val targetDirectory = cipherDirectoryInfoCache.computeIfAbsent(targetDirectoryName) {
            runBlocking {
                fileSystem.findOrCreateDirectory(cipherFilesRootDirectory, targetDirectoryName.toHexString())
            }
        }

        fileSystem.relocate(sourceFile, sourceParentDirectory, targetDirectory)
    }

    //region FileSystemForwarding

    @Throws(Exception::class)
    public suspend fun findOrCreateFile(directoryInfo: DirectoryInfo, name: String): FileInfo {
        return listFiles(directoryInfo).firstOrNull { it.fullName == name }
            ?: fileSystem.createFile(directoryInfo, name)
    }

    public suspend fun findOrCreateDirectory(directoryInfo: DirectoryInfo, name: String): DirectoryInfo {
        return fileSystem.findOrCreateDirectory(directoryInfo, name)
    }

    public fun listFiles(directoryInfo: DirectoryInfo): Flow<FileInfo> {
        return fileSystem.listFiles(directoryInfo)
    }

    public fun listDirectories(directoryInfo: DirectoryInfo): Flow<DirectoryInfo> {
        return fileSystem.listDirectories(directoryInfo)
    }

    public fun delete(fileInfo: FileInfo) {
        return fileSystem.delete(fileInfo)
    }

    public fun openInputStream(fileInfo: FileInfo): FileInputStream {
        return fileSystem.openInputStream(fileInfo)
    }

    public fun openOutputStream(fileInfo: FileInfo): OutputStream {
        return fileSystem.openOutputStream(fileInfo)
    }

    //endregion

    internal companion object {
        private const val CIPHER_FILES_DIRECTORY_NAME: String = "files"
        private const val DECRYPTION_DIRECTORY_NAME: String = "decrypted"

        // Eagerly collecting commonly used directories and files to speed up later access.
        // During vault creation or decryption, the user should be presented with a loading screen anyway.
        internal suspend fun create(
            fileSystem: FileSystem,
            rootDirectory: DirectoryInfo,
            vaultFile: FileInfo
        ): VaultFileSystem {
            val cipherFilesRootDirectory = fileSystem.findOrCreateDirectory(rootDirectory, CIPHER_FILES_DIRECTORY_NAME)
            val folderIndexFile = fileSystem.findOrCreateFile(rootDirectory, "index")

            val cipherDirectoriesById = fileSystem.listDirectories(cipherFilesRootDirectory)
                .toList()
                .mapNotNull { directoryInfo ->
                    Uuid.parseHexOrNull(directoryInfo.name)?.let { id -> id to directoryInfo }
                }
                .toMap()

            return VaultFileSystem(
                fileSystem = fileSystem,
                rootDirectory = rootDirectory,
                cipherFilesRootDirectory = cipherFilesRootDirectory,
                vaultFile = vaultFile,
                folderIndexFile = folderIndexFile,
                cipherDirectoryInfoCache = ConcurrentHashMap(cipherDirectoriesById)
            )
        }
    }
}
