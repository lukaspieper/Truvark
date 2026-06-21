/*
 * SPDX-FileCopyrightText: 2023 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.work

import de.lukaspieper.truvark.domain.IndexHandler
import de.lukaspieper.truvark.domain.entities.CipherFileEntity
import de.lukaspieper.truvark.domain.entities.CipherFolderEntity
import de.lukaspieper.truvark.domain.vault.Vault
import kotlinx.coroutines.flow.first

internal class DeletingWorkBundle(
    override val properties: Properties,
    private val vault: Vault,
    private val folderIndexHandler: IndexHandler.FolderIndexHandler,
    private val fileIndexHandler: IndexHandler.FileIndexHandler,
    private val files: List<CipherFileEntity>,
    private val folders: List<CipherFolderEntity>
) : WorkBundle() {
    override val size: Int = files.size + folders.size

    override suspend fun processUnitAtIndex(index: Int) {
        when {
            index < folders.size -> deleteFoldersRecursively(folders[index])
            index < size -> deleteFile(files[index - folders.size])
        }
    }

    private suspend fun deleteFoldersRecursively(cipherFolderEntity: CipherFolderEntity) {
        vault.findCipherFolderEntitySubfolders(cipherFolderEntity).first().forEach { subfolder ->
            deleteFoldersRecursively(subfolder)
        }

        deleteFolder(cipherFolderEntity)
    }

    private suspend fun deleteFolder(cipherFolderEntity: CipherFolderEntity) {
        folderIndexHandler.deleteItems(cipherFolderEntity)

        vault.fileSystem.findFileIndexFileOrNull(cipherFolderEntity.id)?.let { fileIndexFile ->
            vault.fileSystem.delete(fileIndexFile)
        }
        vault.fileSystem.deleteCipherDirectory(cipherFolderEntity.id)
    }

    private suspend fun deleteFile(file: CipherFileEntity) {
        fileIndexHandler.deleteItems(file)
        vault.fileSystem.deleteFileFromCipherDirectory(file.folderId, file.id)
    }
}
