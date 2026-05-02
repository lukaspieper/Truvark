/*
 * SPDX-FileCopyrightText: 2023 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.domain.vault

import de.lukaspieper.truvark.constants.FileNames
import de.lukaspieper.truvark.constants.FixedValues.MAX_VAULT_NAME_LENGTH
import de.lukaspieper.truvark.test.TestContext
import de.lukaspieper.truvark.test.data.AmountProvider
import de.lukaspieper.truvark.test.data.BlankStringProvider
import de.lukaspieper.truvark.test.data.DisplayNameProvider
import de.lukaspieper.truvark.test.data.FileDataProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource

class VaultTests : TestContext() {

    /**
     * - Using [TestContext.internalDirectory] because it is the only exposed [java.io.File] directory.
     * - Using [FileDataProvider] because [FileNames.INDEX_REALM] would cause a conflict. A subdirectory could be used
     *   instead.
     */
    @Nested
    inner class WriteEncryptedDatabaseCopyTo {

        @ParameterizedTest
        @ArgumentsSource(FileDataProvider::class)
        fun `writeEncryptedDatabaseCopyTo without existing file creates encrypted Copy`(fileName: String) {
            // Arrange
            val destinationFile = internalDirectory.resolve(fileName)

            // Act
            vault.writeEncryptedDatabaseCopyTo(destinationFile)

            // Assert
            assertTrue(destinationFile.length() > 0)
        }

        @ParameterizedTest
        @ArgumentsSource(FileDataProvider::class)
        fun `writeEncryptedDatabaseCopyTo with existing file overwrites existing file`(fileName: String) {
            // Arrange
            val destinationFile = internalDirectory.resolve(fileName)
            destinationFile.createNewFile()

            // Act
            vault.writeEncryptedDatabaseCopyTo(destinationFile)

            // Assert
            assertTrue(destinationFile.length() > 0)
        }
    }

    @Nested
    inner class FindCipherFolderEntitySubfolders {

        @ParameterizedTest
        @ArgumentsSource(AmountProvider::class)
        fun `findCipherFolderEntitySubfolders with given root level folders return same amount`(amountOfFolders: Int) {
            // Arrange
            repeat(amountOfFolders) {
                vault.realm.createRandomCipherFolderEntity()
            }

            // Act
            val rootFolders = runBlocking {
                vault.findCipherFolderEntitySubfolders("").first()
            }

            // Assert
            assertEquals(amountOfFolders, rootFolders.size)
        }

        @ParameterizedTest
        @ArgumentsSource(AmountProvider::class)
        fun `findCipherFolderEntitySubfolders with given subfolders return same amount`(amountOfSubfolders: Int) {
            // Arrange
            val parentFolder = vault.realm.createRandomCipherFolderEntity()

            repeat(amountOfSubfolders) {
                vault.realm.createRandomCipherFolderEntity(parentFolder.id)
            }

            // Act
            val subfolders = runBlocking {
                vault.findCipherFolderEntitySubfolders(parentFolder.id).first()
            }

            // Assert
            assertEquals(amountOfSubfolders, subfolders.size)
        }
    }

    @Nested
    inner class FindCipherFileEntitiesForFolder {

        @ParameterizedTest
        @ArgumentsSource(AmountProvider::class)
        fun `findCipherFileEntitiesForFolder with given fileEntities return same amount`(
            amountOfFileEntities: Int
        ) {
            // Arrange
            val parentFolder = vault.realm.createRandomCipherFolderEntity()

            repeat(amountOfFileEntities) {
                vault.realm.createRandomCipherFileEntity(parentFolder)
            }

            // Act
            val fileEntities = runBlocking {
                vault.findCipherFileEntitiesForFolder(parentFolder.id).first()
            }

            // Assert
            assertEquals(amountOfFileEntities, fileEntities.size)
        }
    }

    @Nested
    inner class UpdateDisplayName {

        @ParameterizedTest
        @ArgumentsSource(DisplayNameProvider::class)
        fun `updateDisplayName updates vault name successfully`(newDisplayName: String) {
            // Act
            vault.updateDisplayName(newDisplayName)

            // Assert
            // Close vault and reopen it to check if the vault config is not corrupted
            vault.realm.close()
            val updatedVault = vaultFactory.decryptVault(
                vaultDirectory = vaultDirectoryInfo,
                password = vaultPassword,
                databaseFile = internalDatabaseFile
            )
            assertEquals(newDisplayName, updatedVault.displayName)
        }

        @Test
        fun `updateDisplayName with unchanged vault name does not fail`() {
            // Act
            vault.updateDisplayName(vault.displayName)

            // Assert
            // Close vault and reopen it to check if the vault config is not corrupted
            vault.realm.close()
            val updatedVault = vaultFactory.decryptVault(
                vaultDirectory = vaultDirectoryInfo,
                password = vaultPassword,
                databaseFile = internalDatabaseFile
            )
            assertEquals(vault.displayName, updatedVault.displayName)
        }

        @ParameterizedTest
        @ArgumentsSource(BlankStringProvider::class)
        fun `updateDisplayName with invalid vault name throws IllegalArgumentException`(invalidVaultName: String) {
            // Act, Assert
            assertThrows<IllegalArgumentException> {
                vault.updateDisplayName(invalidVaultName)
            }
        }

        @Test
        fun `updateDisplayName with too long vault name throws IllegalArgumentException`() {
            // Arrange
            val invalidVaultName = "1".repeat(MAX_VAULT_NAME_LENGTH + 1)

            // Act, Assert
            assertThrows<IllegalArgumentException> {
                vault.updateDisplayName(invalidVaultName)
            }
        }
    }

    @Nested
    inner class RenameFolder {

        @ParameterizedTest
        @ArgumentsSource(DisplayNameProvider::class)
        fun `renameFolder updates folder name successfully`(newFolderName: String) {
            // Arrange
            val folder = vault.realm.createRandomCipherFolderEntity()

            // Act
            runBlocking {
                vault.renameFolder(folder, newFolderName)
            }

            // Assert
            val updatedFolder = runBlocking {
                vault.findCipherFolderEntity(folder.id)
            }
            assertEquals(newFolderName, updatedFolder.displayName)
        }

        @Test
        fun `renameFolder with unchanged folder name does not fail`() {
            // Arrange
            val folder = vault.realm.createRandomCipherFolderEntity()

            // Act, Assert
            assertDoesNotThrow {
                runBlocking {
                    vault.renameFolder(folder, folder.displayName)
                }
            }
        }

        @ParameterizedTest
        @ArgumentsSource(BlankStringProvider::class)
        fun `renameFolder with invalid folder name throws IllegalArgumentException`(invalidFolderName: String) {
            // Arrange
            val folder = vault.realm.createRandomCipherFolderEntity()

            // Act, Assert
            assertThrows<IllegalArgumentException> {
                runBlocking {
                    vault.renameFolder(folder, invalidFolderName)
                }
            }
        }
    }
}
