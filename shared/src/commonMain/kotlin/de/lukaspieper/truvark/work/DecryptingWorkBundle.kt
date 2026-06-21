/*
 * SPDX-FileCopyrightText: 2022 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.work

import de.lukaspieper.truvark.data.io.DirectoryInfo
import de.lukaspieper.truvark.data.io.FileInfo
import de.lukaspieper.truvark.domain.entities.CipherFileEntity
import de.lukaspieper.truvark.domain.entities.CipherFolderEntity
import de.lukaspieper.truvark.domain.entities.OriginalFileMetadata
import de.lukaspieper.truvark.domain.vault.Vault
import de.lukaspieper.truvark.domain.vault.VaultFileSystem
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import logcat.LogPriority.DEBUG
import logcat.LogPriority.INFO
import logcat.LogPriority.WARN
import logcat.logcat
import okio.buffer
import kotlin.system.measureTimeMillis

internal class DecryptingWorkBundle(
    override val properties: Properties,
    private val vault: Vault,
    parentFolder: CipherFolderEntity,
    private val files: List<CipherFileEntity>,
    private val folders: List<CipherFolderEntity>
) : WorkBundle() {
    private val destinationDirectoryInfo by lazy {
        runBlocking { findOrCreateDecryptionDestinationDirectory(parentFolder) }
    }

    override val size: Int = files.size + folders.size

    override suspend fun processUnitAtIndex(index: Int) {
        when {
            index < folders.size -> {
                decryptFoldersRecursively(folders[index], destinationDirectoryInfo)
            }

            index < size -> {
                decryptFile(files[index - folders.size], destinationDirectoryInfo)
            }
        }
    }

    /**
     * Returns a [DirectoryInfo] that matches the folder hierarchy of the given [folder]. All parent directories will be
     * created if they do not exist yet. [VaultFileSystem.decryptionRootDirectory] is used as root directory.
     *
     * **NOTE: [folder] must be the parent folder of the files and folders that should be decrypted in the next step.**
     */
    private suspend fun findOrCreateDecryptionDestinationDirectory(folder: CipherFolderEntity): DirectoryInfo {
        return when {
            folder.id == vault.rootFolder.id -> vault.fileSystem.decryptionRootDirectory
            else -> {
                val parentFolder = vault.findCipherFolderEntity(folder.parentFolderId)
                val directory = findOrCreateDecryptionDestinationDirectory(parentFolder)

                vault.fileSystem.findOrCreateDirectory(directory, folder.displayName)
            }
        }
    }

    /**
     * Finds the physical file for [cipherFileEntity]. Reads and decrypts all bytes from it and writes them to a new
     * file on disk. The new file is located in [destinationDirectory]. The primary source for the name of the decrypted
     * file is [cipherFileEntity], in case that is not available the name will be taken from the encrypted file's header
     * (see [OriginalFileMetadata]). As a final fallback the name (id) of the encrypted file will be used.
     */
    private suspend fun decryptFile(
        cipherFileEntity: CipherFileEntity,
        destinationDirectory: DirectoryInfo
    ) {
        val cipherFile = vault.fileSystem.findFileInCipherDirectory(cipherFileEntity.folderId, cipherFileEntity.id)!!
        decryptToFile(cipherFile, cipherFileEntity, destinationDirectory)
    }

    /**
     * Decrypts all files in [cipherFolderEntity] and all its subfolders recursively. The matching directory hierarchy
     * will be created in [parentDecryptionDirectory].
     */
    private suspend fun decryptFoldersRecursively(
        cipherFolderEntity: CipherFolderEntity,
        parentDecryptionDirectory: DirectoryInfo
    ) {
        val currentDecryptionDirectory = vault.fileSystem.findOrCreateDirectory(
            directoryInfo = parentDecryptionDirectory,
            name = cipherFolderEntity.displayName
        )

        vault.findCipherFolderEntitySubfolders(cipherFolderEntity).first().forEach { subfolder ->
            decryptFoldersRecursively(subfolder, currentDecryptionDirectory)
        }

        decryptDirectory(cipherFolderEntity, currentDecryptionDirectory)
    }

    private suspend fun decryptDirectory(cipherFolderEntity: CipherFolderEntity, decryptionDirectory: DirectoryInfo) {
        vault.fileSystem.listFilesInCipherDirectory(cipherFolderEntity.id).collect { file ->
            // TODO: Get and pass down the index entry
            decryptToFile(file, null, decryptionDirectory)
        }
    }

    private suspend fun decryptToFile(
        cipherFile: FileInfo,
        cipherFileEntity: CipherFileEntity?,
        destinationDirectory: DirectoryInfo
    ) {
        logcat(INFO) { "Start decrypting cipher file to disk." }

        vault.createDecryptingFileHandle(cipherFile).use { decryptingFileHandle ->
            val elapsedMilliseconds = measureTimeMillis {
                val destinationFile = createAppropriateDestinationFile(
                    destinationDir = destinationDirectory,
                    cipherFileEntity = cipherFileEntity,
                    originalFileMetadata = decryptingFileHandle.metadata,
                    fallbackFileName = cipherFile.fullName
                )
                decryptingFileHandle.source().buffer().inputStream().use { inputStream ->
                    vault.fileSystem.openOutputStream(destinationFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
            }

            logcat(INFO) { "Decryption to disk finished in $elapsedMilliseconds milliseconds." }
        }
    }

    private suspend fun createAppropriateDestinationFile(
        destinationDir: DirectoryInfo,
        cipherFileEntity: CipherFileEntity?,
        originalFileMetadata: OriginalFileMetadata,
        fallbackFileName: String
    ): FileInfo {
        val fileName = when {
            cipherFileEntity?.fullName?.isNotBlank() == true -> {
                logcat(DEBUG) { "Using index entry for decryption file name." }
                cipherFileEntity.fullName
            }

            originalFileMetadata.fullName.isNotBlank() -> {
                logcat(DEBUG) { "Using encrypted file header for decryption file name." }
                originalFileMetadata.fullName
            }

            else -> {
                logcat(WARN) { "Neither index entry nor encrypted file header could be used! Using fallback name." }
                fallbackFileName
            }
        }

        return vault.fileSystem.findOrCreateFile(destinationDir, fileName)
    }
}
