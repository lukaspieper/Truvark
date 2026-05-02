/*
 * SPDX-FileCopyrightText: 2024 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.work

import de.lukaspieper.truvark.domain.IndexHandler
import de.lukaspieper.truvark.domain.entities.CipherFileEntity
import de.lukaspieper.truvark.domain.entities.CipherFolderEntity
import de.lukaspieper.truvark.domain.vault.VaultFileSystem

internal class RelocatingWorkBundle(
    override val properties: Properties,
    private val fileSystem: VaultFileSystem,
    private val folderIndexHandler: IndexHandler.FolderIndexHandler,
    private val sourceFileIndexHandler: IndexHandler.FileIndexHandler,
    private val destinationFileIndexHandler: IndexHandler.FileIndexHandler,
    private val destination: CipherFolderEntity,
    private val files: List<CipherFileEntity>,
    private val folders: List<CipherFolderEntity>
) : WorkBundle() {
    override val size: Int = files.size + folders.size

    override suspend fun processUnitAtIndex(index: Int) {
        when {
            index < folders.size -> relocateFolder(folders[index])
            index < size -> relocateFile(files[index - folders.size], destination)
        }
    }

    private suspend fun relocateFolder(folder: CipherFolderEntity) {
        val updatedFolder = folder.copy(parentFolderId = destination.id)
        folderIndexHandler.updateItem(folder, updatedFolder)
    }

    internal suspend fun relocateFile(file: CipherFileEntity, destinationFolder: CipherFolderEntity) {
        // TODO: Previous implementation had recovery steps. However not sure how to construct a test for it.
        sourceFileIndexHandler.deleteItems(file)

        fileSystem.relocateFileIntoCipherDirectory(file.id, file.folderId, destinationFolder.id)

        file.folderId = destinationFolder.id
        destinationFileIndexHandler.addItems(file)
    }
}
