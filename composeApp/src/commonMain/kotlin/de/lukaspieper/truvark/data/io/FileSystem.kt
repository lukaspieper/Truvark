/*
 * SPDX-FileCopyrightText: 2023 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.data.io

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.OutputStream

/**
 * A central point of access to a file system. Note that the returned [FileInfo] and [DirectoryInfo] objects may only be
 * used with the same [FileSystem] implementation. Other implementations will likely throw exceptions.
 */
public abstract class FileSystem {

    @Throws(Exception::class)
    public abstract suspend fun createFile(
        directoryInfo: DirectoryInfo,
        name: String,
        mimeType: String = "application/octet-stream"
    ): FileInfo

    public open suspend fun findFileOrNull(directoryInfo: DirectoryInfo, name: String): FileInfo? {
        return listFiles(directoryInfo).firstOrNull { it.fullName == name }
    }

    @Throws(Exception::class)
    public open suspend fun findOrCreateFile(directoryInfo: DirectoryInfo, name: String): FileInfo {
        return findFileOrNull(directoryInfo, name) ?: createFile(directoryInfo, name)
    }

    protected abstract fun createDirectory(directoryInfo: DirectoryInfo, name: String): DirectoryInfo

    public open suspend fun findDirectoryOrNull(directoryInfo: DirectoryInfo, name: String): DirectoryInfo? {
        return listDirectories(directoryInfo).firstOrNull { it.name == name }
    }

    @Throws(Exception::class)
    public open suspend fun findOrCreateDirectory(directoryInfo: DirectoryInfo, name: String): DirectoryInfo {
        return findDirectoryOrNull(directoryInfo, name) ?: createDirectory(directoryInfo, name)
    }

    public abstract fun listFiles(directoryInfo: DirectoryInfo): Flow<FileInfo>
    public abstract fun listDirectories(directoryInfo: DirectoryInfo): Flow<DirectoryInfo>

    @Throws(Exception::class)
    public abstract fun delete(fileInfo: FileInfo)

    @Throws(Exception::class)
    public abstract fun delete(directoryInfo: DirectoryInfo)

    public abstract fun relocate(
        sourceFileInfo: FileInfo,
        sourceParentDirectoryInfo: DirectoryInfo,
        targetDirectoryInfo: DirectoryInfo
    )

    @Throws(FileNotFoundException::class)
    public abstract fun openInputStream(fileInfo: FileInfo): FileInputStream

    @Throws(FileNotFoundException::class)
    public abstract fun openOutputStream(fileInfo: FileInfo): OutputStream
}
