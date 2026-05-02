/*
 * SPDX-FileCopyrightText: 2023 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.domain.vault.internal

import de.lukaspieper.truvark.domain.entities.CipherFolderEntity
import de.lukaspieper.truvark.domain.entities.RealmCipherFolderEntity
import de.lukaspieper.truvark.domain.vault.Vault
import de.lukaspieper.truvark.test.TestContext
import de.lukaspieper.truvark.test.data.DisplayNameProvider
import de.lukaspieper.truvark.test.data.FileDataProvider
import de.lukaspieper.truvark.test.doubles.SchedulerFake
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource
import kotlin.uuid.Uuid

/**
 * Tests for [Vault.scheduleDecryption] and the underlying [FileDecryption].
 */
class ScheduleDecryptionTests : TestContext() {

    @Nested
    inner class Files {

        @Test
        fun `scheduleDecryption without physical file throws Exception`() {
            // Arrange
            val parentFolder = vault.realm.createRandomCipherFolderEntity()
            val cipherFileEntity = vault.realm.createRandomCipherFileEntity(parentFolder)

            // Act, Assert
            assertThrows<Exception> {
                vault.scheduleDecryption(
                    metadata = SchedulerFake.MetadataFake,
                    parentFolder = parentFolder,
                    files = listOf(cipherFileEntity),
                    folders = emptyList()
                )
            }
        }

        @ParameterizedTest
        @ArgumentsSource(FileDataProvider::class)
        fun `scheduleDecryption uses database entry for successful decryption`(
            fileDisplayName: String,
            fileLength: Int
        ) {
            // Arrange
            val parentFolder = vault.realm.createRandomCipherFolderEntity()
            val fileToEncrypt = fileSystem.createFile(sharedDirectoryInfo, fileDisplayName)
                .withData(ByteArray(fileLength) { it.toByte() })

            vault.scheduleFileEncryption(SchedulerFake.MetadataFake, listOf { fileToEncrypt }, parentFolder)
            val cipherFileEntity = vault.realm.findCipherFileEntityByName(fileDisplayName).single()

            // Act
            vault.scheduleDecryption(SchedulerFake.MetadataFake, parentFolder, listOf(cipherFileEntity), emptyList())

            // Assert
            val destinationFile = assertDoesNotThrow {
                vaultDecryptionDirectoryInfo.resolveFileInfo(parentFolder.displayName, fileDisplayName)
            }
            assertArrayEquals(ByteArray(fileLength) { it.toByte() }, destinationFile.readBytes())
        }

        @ParameterizedTest
        @ArgumentsSource(FileDataProvider::class)
        fun `scheduleDecryption uses encrypted file's header for successful decryption`(
            fileDisplayName: String,
            fileLength: Int
        ) {
            // Arrange
            val parentFolder = vault.realm.createRandomCipherFolderEntity()
            val fileToEncrypt = fileSystem.createFile(sharedDirectoryInfo, fileDisplayName)
                .withData(ByteArray(fileLength) { it.toByte() })

            vault.scheduleFileEncryption(SchedulerFake.MetadataFake, listOf { fileToEncrypt }, parentFolder)
            val cipherFileEntity = vault.realm.findCipherFileEntityByName(fileDisplayName).single()

            vault.realm.writeBlocking { findLatest(cipherFileEntity)?.let { delete(it) } }

            // Act
            vault.scheduleDecryption(SchedulerFake.MetadataFake, parentFolder, listOf(cipherFileEntity), emptyList())

            // Assert
            val destinationFile = assertDoesNotThrow {
                vaultDecryptionDirectoryInfo.resolveFileInfo(parentFolder.displayName, fileDisplayName)
            }
            assertArrayEquals(ByteArray(fileLength) { it.toByte() }, destinationFile.readBytes())
        }
    }

    @Nested
    inner class Folders {

        @ParameterizedTest
        @ArgumentsSource(DisplayNameProvider::class)
        fun `scheduleDecryption with invalid CipherFolderEntity throws IllegalStateException`(
            folderDisplayName: String
        ) {
            // Arrange
            val rootFolder = runBlocking { vault.findCipherFolderEntity("") }
            val folder = RealmCipherFolderEntity().apply {
                id = Uuid.random().toHexString()
                displayName = folderDisplayName
            }

            // Act, Assert
            assertThrows<IllegalStateException> {
                vault.scheduleDecryption(SchedulerFake.MetadataFake, rootFolder, emptyList(), listOf(folder))
            }
        }

        @ParameterizedTest
        @ArgumentsSource(FileDataProvider::class)
        fun `scheduleDecryption with subfolder-less CipherFolderEntity decrypts successfully`(
            fileDisplayName: String,
            fileLength: Int
        ) {
            // Arrange
            val rootFolder = runBlocking { vault.findCipherFolderEntity("") }
            val folder = vault.realm.createRandomCipherFolderEntity()

            val fileToEncrypt = fileSystem.createFile(sharedDirectoryInfo, fileDisplayName)
                .withData(ByteArray(fileLength) { it.toByte() })
            vault.scheduleFileEncryption(SchedulerFake.MetadataFake, listOf { fileToEncrypt }, folder)

            // Act
            vault.scheduleDecryption(SchedulerFake.MetadataFake, rootFolder, emptyList(), listOf(folder))

            // Assert
            val destinationFile = assertDoesNotThrow {
                vaultDecryptionDirectoryInfo.resolveFileInfo(folder.displayName, fileDisplayName)
            }
            assertArrayEquals(ByteArray(fileLength) { it.toByte() }, destinationFile.readBytes())
        }

        @ParameterizedTest
        @ArgumentsSource(FileDataProvider::class)
        fun `scheduleDecryption with CipherFolderEntity having subfolders decrypts successfully`(
            fileDisplayName: String,
            fileLength: Int
        ) {
            // Arrange
            val rootFolder = runBlocking { vault.findCipherFolderEntity("") }
            var folder: CipherFolderEntity = vault.realm.createRandomCipherFolderEntity()
            val subfolder = vault.realm.createRandomCipherFolderEntity(folder.id)

            val fileToEncrypt = fileSystem.createFile(sharedDirectoryInfo, fileDisplayName)
                .withData(ByteArray(fileLength) { it.toByte() })
            vault.scheduleFileEncryption(SchedulerFake.MetadataFake, listOf { fileToEncrypt }, subfolder)

            // Refresh frozen folder object with updated one
            folder = runBlocking { vault.findCipherFolderEntity(folder.id) }

            // Act
            vault.scheduleDecryption(SchedulerFake.MetadataFake, rootFolder, emptyList(), listOf(folder))

            // Assert
            val destinationFile = assertDoesNotThrow {
                vaultDecryptionDirectoryInfo.resolveFileInfo(folder.displayName, subfolder.displayName, fileDisplayName)
            }
            assertArrayEquals(ByteArray(fileLength) { it.toByte() }, destinationFile.readBytes())
        }

        @ParameterizedTest
        @ArgumentsSource(FileDataProvider::class)
        fun `scheduleDecryption targeting subfolder decrypts successfully`(fileDisplayName: String, fileLength: Int) {
            // Arrange
            val folder = vault.realm.createRandomCipherFolderEntity()
            val subfolder = vault.realm.createRandomCipherFolderEntity(folder.id)

            val fileToEncrypt = fileSystem.createFile(sharedDirectoryInfo, fileDisplayName)
                .withData(ByteArray(fileLength) { it.toByte() })
            vault.scheduleFileEncryption(SchedulerFake.MetadataFake, listOf { fileToEncrypt }, subfolder)

            // Act
            vault.scheduleDecryption(SchedulerFake.MetadataFake, folder, emptyList(), listOf(subfolder))

            // Assert
            val destinationFile = assertDoesNotThrow {
                vaultDecryptionDirectoryInfo.resolveFileInfo(folder.displayName, subfolder.displayName, fileDisplayName)
            }
            assertArrayEquals(ByteArray(fileLength) { it.toByte() }, destinationFile.readBytes())
        }

        @ParameterizedTest
        @ArgumentsSource(FileDataProvider::class)
        fun `scheduleDecryption targeting sub-subfolder decrypts successfully`(
            fileDisplayName: String,
            fileLength: Int
        ) {
            // Arrange
            val folder = vault.realm.createRandomCipherFolderEntity()
            val subfolder1 = vault.realm.createRandomCipherFolderEntity(folder.id)
            val subfolder2 = vault.realm.createRandomCipherFolderEntity(subfolder1.id)

            val fileToEncrypt = fileSystem.createFile(sharedDirectoryInfo, fileDisplayName)
                .withData(ByteArray(fileLength) { it.toByte() })
            vault.scheduleFileEncryption(SchedulerFake.MetadataFake, listOf { fileToEncrypt }, subfolder2)

            // Act
            vault.scheduleDecryption(SchedulerFake.MetadataFake, subfolder1, emptyList(), listOf(subfolder2))

            // Assert
            val destinationFile = assertDoesNotThrow {
                vaultDecryptionDirectoryInfo.resolveFileInfo(
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
