/*
 * SPDX-FileCopyrightText: 2025 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.common.test

import de.lukaspieper.truvark.common.data.io.DirectoryInfo
import de.lukaspieper.truvark.common.data.io.FileInfo
import de.lukaspieper.truvark.common.data.io.JavaFileSystem
import java.io.File

internal interface IoExtensions {

    val fileSystem: JavaFileSystem

    fun FileInfo.withData(bytes: ByteArray): FileInfo {
        (uri as File).writeBytes(bytes)

        // Refresh the file info because the size has changed
        return fileSystem.fileInfo(uri as File)
    }

    fun FileInfo.readBytes(): ByteArray? {
        return (uri as? File)?.readBytes()
    }

    @Throws(IllegalArgumentException::class)
    fun DirectoryInfo.resolveFileInfo(vararg relativePathElements: String): FileInfo {
        val resolvedFile = (uri as File).resolve(relativePathElements.joinToString(File.separator))
        return fileSystem.fileInfo(resolvedFile)
    }

    fun JavaFileSystem.exists(fileInfo: FileInfo): Boolean {
        return (fileInfo.uri as File).exists()
    }
}
