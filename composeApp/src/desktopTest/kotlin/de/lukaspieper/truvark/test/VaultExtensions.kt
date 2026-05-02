/*
 * SPDX-FileCopyrightText: 2024 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.test

import com.google.crypto.tink.StreamingAead
import de.lukaspieper.truvark.data.io.DirectoryInfo
import de.lukaspieper.truvark.domain.IndexHandler
import de.lukaspieper.truvark.domain.entities.CipherFileEntity
import de.lukaspieper.truvark.domain.entities.CipherFolderEntity
import de.lukaspieper.truvark.domain.vault.Vault
import kotlinx.coroutines.runBlocking
import kotlin.time.Clock
import kotlin.uuid.Uuid

internal interface VaultExtensions {

    val vaultDirectoryInfo: DirectoryInfo
    val vaultStreamingAead: StreamingAead

    val Vault.cipherFilesDirectoryInfo: DirectoryInfo
        get() = runBlocking { fileSystem.findOrCreateDirectory(vaultDirectoryInfo, "files") } // Constant is private

    suspend fun Vault.createRandomCipherFolderEntity(
        parentFolder: CipherFolderEntity = rootFolder,
        displayName: String? = null
    ): CipherFolderEntity {
        val folder = createRandomCipherFolderEntities(1, parentFolder).single()

        return when {
            displayName != null -> {
                val indexHandler = IndexHandler.FolderIndexHandler(vaultStreamingAead, fileSystem, config)

                val updatedFolder = folder.copy(displayName = displayName)
                indexHandler.updateItem(folder, updatedFolder)

                updatedFolder
            }

            else -> folder
        }
    }

    suspend fun Vault.createRandomCipherFolderEntities(
        amount: Int,
        parentFolder: CipherFolderEntity = rootFolder
    ): List<CipherFolderEntity> {
        val indexHandler = IndexHandler.FolderIndexHandler(vaultStreamingAead, fileSystem, config)

        val folders = (0..<amount).map { index ->
            CipherFolderEntity(
                id = Uuid.random(),
                displayName = index.toPaddedString(),
                parentFolderId = parentFolder.id,
                creationTimestamp = Clock.System.now()
            )
        }

        indexHandler.addItems(*folders.toTypedArray())
        return folders
    }

    suspend fun Vault.createRandomCipherFileEntity(parentFolder: CipherFolderEntity): CipherFileEntity {
        return createRandomCipherFileEntities(1, parentFolder).single()
    }

    suspend fun Vault.createRandomCipherFileEntities(
        amount: Int,
        parentFolder: CipherFolderEntity
    ): List<CipherFileEntity> {
        val indexHandler = IndexHandler.FileIndexHandler(
            streamingAead = vaultStreamingAead,
            vaultFileSystem = fileSystem,
            vaultConfig = config,
            folder = parentFolder
        )

        val files = (0..<amount).map { index ->
            CipherFileEntity(
                id = Uuid.random(),
                fullName = "${index.toPaddedString()}.file",
                mimeType = "application/octet-stream",
                fileSize = 0L,
                mediaDuration = null,
                creationTimestamp = Clock.System.now(),
                folderId = parentFolder.id
            ).apply {
                sha256Digest = ByteArray(32)
            }
        }

        indexHandler.addItems(*files.toTypedArray())
        return files
    }

    fun Int.toPaddedString(): String {
        return this.toString().padStart(3, '0')
    }
}
