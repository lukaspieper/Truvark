/*
 * SPDX-FileCopyrightText: 2023 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.domain.vault

import de.lukaspieper.truvark.domain.vault.VaultConfig.Companion.MAX_VAULT_NAME_LENGTH
import de.lukaspieper.truvark.test.TestContext
import de.lukaspieper.truvark.test.data.AmountProvider
import de.lukaspieper.truvark.test.data.BlankStringProvider
import de.lukaspieper.truvark.test.data.DisplayNameProvider
import kotlinx.coroutines.flow.first
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource

class VaultTests : TestContext() {

    @Nested
    inner class FindCipherFolderEntitySubfolders {

        @ParameterizedTest
        @ArgumentsSource(AmountProvider::class)
        suspend fun `findCipherFolderEntitySubfolders with given root level folders return matching list`(
            amountOfFolders: Int
        ) {
            // Arrange
            val createdFolders = vault.createRandomCipherFolderEntities(amountOfFolders)
            reloadVault()

            // Act
            val rootFolders = vault.findCipherFolderEntitySubfolders(vault.rootFolder).first()

            // Assert
            assertEquals(createdFolders.sortedBy { it.id }, rootFolders.sortedBy { it.id })
        }

        @ParameterizedTest
        @ArgumentsSource(AmountProvider::class)
        suspend fun `findCipherFolderEntitySubfolders with given subfolders return matching list`(
            amountOfSubfolders: Int
        ) {
            // Arrange
            val parentFolder = vault.createRandomCipherFolderEntity()
            val createdSubfolders = vault.createRandomCipherFolderEntities(amountOfSubfolders, parentFolder)
            reloadVault()

            // Act
            val subfolders = vault.findCipherFolderEntitySubfolders(parentFolder).first()

            // Assert
            assertEquals(createdSubfolders.sortedBy { it.id }, subfolders.sortedBy { it.id })
        }
    }

    @Nested
    inner class FindCipherFileEntitiesForFolder {

        @ParameterizedTest
        @ArgumentsSource(AmountProvider::class)
        suspend fun `findCipherFileEntitiesForFolder with given fileEntities return same amount`(
            amountOfFileEntities: Int
        ) {
            // Arrange
            val parentFolder = vault.createRandomCipherFolderEntity()
            val createdFiles = vault.createRandomCipherFileEntities(amountOfFileEntities, parentFolder)

            // Act
            val fileEntities = vault.findCipherFileEntitiesForFolder(parentFolder).first()

            // Assert
            assertTrue { fileEntities.all { file -> file.folderId == parentFolder.id } }
            assertEquals(createdFiles.sortedBy { it.id }, fileEntities.sortedBy { it.id })
        }
    }

    @Nested
    inner class UpdateName {

        @ParameterizedTest
        @ArgumentsSource(DisplayNameProvider::class)
        fun `updateName updates name successfully`(updatedName: String) {
            // Act
            vault.updateName(updatedName)

            // Assert
            reloadVault()
            assertEquals(updatedName, vault.name)
        }

        @Test
        fun `updateName with unchanged name does not fail`() {
            // Act
            val vaultName = vault.name
            vault.updateName(vaultName)

            // Assert
            reloadVault()
            assertEquals(vaultName, vault.name)
        }

        @ParameterizedTest
        @ArgumentsSource(BlankStringProvider::class)
        fun `updateName with invalid name throws IllegalArgumentException`(invalidName: String) {
            // Act, Assert
            assertThrows<IllegalArgumentException> {
                vault.updateName(invalidName)
            }
        }

        @Test
        fun `updateName with too long name throws IllegalArgumentException`() {
            // Arrange
            val invalidName = "1".repeat(MAX_VAULT_NAME_LENGTH + 1)

            // Act, Assert
            assertThrows<IllegalArgumentException> {
                vault.updateName(invalidName)
            }
        }
    }

    @Nested
    inner class RenameFolder {

        @ParameterizedTest
        @ArgumentsSource(DisplayNameProvider::class)
        suspend fun `renameFolder updates folder name successfully`(displayName: String) {
            // Arrange
            val folder = vault.createRandomCipherFolderEntity()
            reloadVault() // cannot rename "unknown" folders, must be in cache.

            // Act
            vault.renameFolder(folder, displayName)

            // Assert
            reloadVault() // to ensure data is persisted to disk.

            val folderFromDisk = vault.findCipherFolderEntity(folder.id)
            assertEquals(displayName, folderFromDisk.displayName)
        }

        @ParameterizedTest
        @ArgumentsSource(DisplayNameProvider::class)
        suspend fun `renameFolder with unchanged folder name does not fail`(displayName: String) {
            // Arrange
            val folder = vault.createFolder(displayName, vault.rootFolder)

            // Act, Assert
            assertDoesNotThrow {
                vault.renameFolder(folder, folder.displayName)
            }
        }

        @ParameterizedTest
        @ArgumentsSource(BlankStringProvider::class)
        suspend fun `renameFolder with invalid folder name throws IllegalArgumentException`(invalidFolderName: String) {
            // Arrange
            val folder = vault.createRandomCipherFolderEntity()

            // Act, Assert
            assertThrows<IllegalArgumentException> {
                vault.renameFolder(folder, invalidFolderName)
            }
        }
    }
}
