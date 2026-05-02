/*
 * SPDX-FileCopyrightText: 2023 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.domain.vault.work

import de.lukaspieper.truvark.domain.vault.Vault
import de.lukaspieper.truvark.test.TestContext
import de.lukaspieper.truvark.test.data.FileDataProvider
import de.lukaspieper.truvark.test.doubles.SchedulerFake
import de.lukaspieper.truvark.work.DecryptingWorkBundle
import kotlinx.coroutines.flow.first
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource

/**
 * Tests for [Vault.scheduleDecryption] and the underlying [DecryptingWorkBundle].
 */
class ScheduleDecryptionTests : TestContext() {

    @Nested
    inner class Files {

        @Test
        suspend fun `scheduleDecryption without physical file throws Exception`() {
            // Arrange
            val parentFolder = vault.createRandomCipherFolderEntity()
            val cipherFileEntity = vault.createRandomCipherFileEntity(parentFolder)

            // Act, Assert
            assertThrows<Exception> {
                vault.scheduleDecryption(
                    properties = SchedulerFake.EmptyProperties,
                    files = setOf(cipherFileEntity),
                    folders = emptySet()
                )
            }
        }

        @ParameterizedTest
        @ArgumentsSource(FileDataProvider::class)
        suspend fun `scheduleDecryption uses index entry for successful decryption`(
            fileDisplayName: String,
            fileLength: Int
        ) {
            // Arrange
            val parentFolder = vault.createRandomCipherFolderEntity(displayName = "folder")
            val fileToEncrypt = fileSystem.createFile(sharedDirectoryInfo, fileDisplayName)
                .withData(ByteArray(fileLength) { it.toByte() })

            vault.scheduleFileEncryption(SchedulerFake.EmptyProperties, listOf { fileToEncrypt }, parentFolder)
            val cipherFileEntity = vault.findCipherFileEntitiesForFolder(parentFolder).first().single()

            // Act
            vault.scheduleDecryption(SchedulerFake.EmptyProperties, setOf(cipherFileEntity), emptySet())

            // Assert
            val destinationFile = assertDoesNotThrow {
                vault.fileSystem.decryptionRootDirectory.resolveFileInfo(parentFolder.displayName, fileDisplayName)
            }
            assertArrayEquals(ByteArray(fileLength) { it.toByte() }, destinationFile.readBytes())
        }
    }

    @Nested
    inner class Folders {

        @ParameterizedTest
        @ArgumentsSource(FileDataProvider::class)
        suspend fun `scheduleDecryption with subfolder-less CipherFolderEntity decrypts successfully`(
            fileDisplayName: String,
            fileLength: Int
        ) {
            // Arrange
            val folder = vault.createRandomCipherFolderEntity(displayName = "folder")
            val fileToEncrypt = fileSystem.createFile(sharedDirectoryInfo, fileDisplayName)
                .withData(ByteArray(fileLength) { it.toByte() })

            vault.scheduleFileEncryption(SchedulerFake.EmptyProperties, listOf { fileToEncrypt }, folder)

            // Act
            vault.scheduleDecryption(SchedulerFake.EmptyProperties, emptySet(), setOf(folder))

            // Assert
            val destinationFile = assertDoesNotThrow {
                vault.fileSystem.decryptionRootDirectory.resolveFileInfo(folder.displayName, fileDisplayName)
            }
            assertArrayEquals(ByteArray(fileLength) { it.toByte() }, destinationFile.readBytes())
        }

        /**
         * File names are converted to UUIDs at some point. This test ensures that the decryption still works even when
         * for some reason other files are located in the vault's cipher directories.
         */
        @ParameterizedTest
        @ArgumentsSource(FileDataProvider::class)
        suspend fun `scheduleDecryption with non-uuid-named file does not fail`(
            fileDisplayName: String,
            fileLength: Int
        ) {
            // Arrange
            val folder = vault.createRandomCipherFolderEntity(displayName = "folder")
            val fileToEncrypt = fileSystem.createFile(sharedDirectoryInfo, fileDisplayName)
                .withData(ByteArray(fileLength) { it.toByte() })

            vault.scheduleFileEncryption(SchedulerFake.EmptyProperties, listOf { fileToEncrypt }, folder)

            fileSystem.findDirectoryOrNull(vault.cipherFilesDirectoryInfo, folder.id.toHexString())!!.let {
                fileSystem.createFile(it, "non-uuid-file")
            }

            // Act
            vault.scheduleDecryption(SchedulerFake.EmptyProperties, emptySet(), setOf(folder))

            // Assert
            val destinationFile = assertDoesNotThrow {
                vault.fileSystem.decryptionRootDirectory.resolveFileInfo(folder.displayName, fileDisplayName)
            }
            assertArrayEquals(ByteArray(fileLength) { it.toByte() }, destinationFile.readBytes())
        }

        @ParameterizedTest
        @ArgumentsSource(FileDataProvider::class)
        suspend fun `scheduleDecryption with CipherFolderEntity having subfolders decrypts successfully`(
            fileDisplayName: String,
            fileLength: Int
        ) {
            // Arrange
            val folder = vault.createRandomCipherFolderEntity(displayName = "folder")
            val subfolder = vault.createRandomCipherFolderEntity(folder, displayName = "subfolder")

            val fileToEncrypt = fileSystem.createFile(sharedDirectoryInfo, fileDisplayName)
                .withData(ByteArray(fileLength) { it.toByte() })

            vault.scheduleFileEncryption(SchedulerFake.EmptyProperties, listOf { fileToEncrypt }, subfolder)

            // Act
            vault.scheduleDecryption(SchedulerFake.EmptyProperties, emptySet(), setOf(folder))

            // Assert
            val destinationFile = assertDoesNotThrow {
                vault.fileSystem.decryptionRootDirectory.resolveFileInfo(
                    folder.displayName,
                    subfolder.displayName,
                    fileDisplayName
                )
            }
            assertArrayEquals(ByteArray(fileLength) { it.toByte() }, destinationFile.readBytes())
        }

        @ParameterizedTest
        @ArgumentsSource(FileDataProvider::class)
        suspend fun `scheduleDecryption targeting subfolder decrypts successfully`(
            fileDisplayName: String,
            fileLength: Int
        ) {
            // Arrange
            val folder = vault.createRandomCipherFolderEntity(displayName = "folder")
            val subfolder = vault.createRandomCipherFolderEntity(folder, displayName = "subfolder")

            val fileToEncrypt = fileSystem.createFile(sharedDirectoryInfo, fileDisplayName)
                .withData(ByteArray(fileLength) { it.toByte() })

            vault.scheduleFileEncryption(SchedulerFake.EmptyProperties, listOf { fileToEncrypt }, subfolder)

            // Act
            vault.scheduleDecryption(SchedulerFake.EmptyProperties, emptySet(), setOf(subfolder))

            // Assert
            val destinationFile = assertDoesNotThrow {
                vault.fileSystem.decryptionRootDirectory.resolveFileInfo(
                    folder.displayName,
                    subfolder.displayName,
                    fileDisplayName
                )
            }
            assertArrayEquals(ByteArray(fileLength) { it.toByte() }, destinationFile.readBytes())
        }

        @ParameterizedTest
        @ArgumentsSource(FileDataProvider::class)
        suspend fun `scheduleDecryption targeting sub-subfolder decrypts successfully`(
            fileDisplayName: String,
            fileLength: Int
        ) {
            // Arrange
            val folder = vault.createRandomCipherFolderEntity(displayName = "folder")
            val subfolder1 = vault.createRandomCipherFolderEntity(folder, displayName = "subfolder1")
            val subfolder2 = vault.createRandomCipherFolderEntity(subfolder1, displayName = "subfolder2")

            val fileToEncrypt = fileSystem.createFile(sharedDirectoryInfo, fileDisplayName)
                .withData(ByteArray(fileLength) { it.toByte() })

            vault.scheduleFileEncryption(SchedulerFake.EmptyProperties, listOf { fileToEncrypt }, subfolder2)

            // Act
            vault.scheduleDecryption(SchedulerFake.EmptyProperties, emptySet(), setOf(subfolder2))

            // Assert
            val destinationFile = assertDoesNotThrow {
                vault.fileSystem.decryptionRootDirectory.resolveFileInfo(
                    folder.displayName,
                    subfolder1.displayName,
                    subfolder2.displayName,
                    fileDisplayName
                )
            }
            assertArrayEquals(ByteArray(fileLength) { it.toByte() }, destinationFile.readBytes())
        }
    }
}
