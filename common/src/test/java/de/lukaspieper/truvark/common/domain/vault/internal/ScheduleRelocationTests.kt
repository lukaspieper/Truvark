/*
 * SPDX-FileCopyrightText: 2024 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.common.domain.vault.internal

import de.lukaspieper.truvark.common.domain.entities.RealmCipherFolderEntity
import de.lukaspieper.truvark.common.domain.vault.Vault
import de.lukaspieper.truvark.common.test.TestContext
import de.lukaspieper.truvark.common.test.data.DisplayNameProvider
import de.lukaspieper.truvark.common.test.data.FileDataProvider
import de.lukaspieper.truvark.common.test.doubles.SchedulerFake
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Tests for [Vault.scheduleRelocation] and the underlying [FileRelocation].
 */
class ScheduleRelocationTests : TestContext() {

    @Nested
    inner class Files {

        @ParameterizedTest
        @ArgumentsSource(FileDataProvider::class)
        fun `scheduleRelocation with single file is successful`(fileDisplayName: String, fileLength: Int) {
            // Arrange
            val parentFolder = vault.realm.createRandomCipherFolderEntity()

            val fileToEncrypt = fileSystem.createFile(sharedDirectoryInfo, fileDisplayName)
                .withData(ByteArray(fileLength) { it.toByte() })
            vault.scheduleFileEncryption(SchedulerFake.MetadataFake, listOf { fileToEncrypt }, parentFolder)

            val cipherFileEntity = vault.realm.findCipherFileEntityByName(fileDisplayName).single()
            val destinationFolder = vault.realm.createRandomCipherFolderEntity()

            // Act
            vault.scheduleRelocation(
                metadata = SchedulerFake.MetadataFake,
                destination = destinationFolder,
                files = listOf(cipherFileEntity),
                folders = emptyList()
            )

            // Assert
            val originFileEntities = runBlocking { vault.findCipherFileEntitiesForFolder(parentFolder.id).first() }
            val destinationFileEntities = runBlocking {
                vault.findCipherFileEntitiesForFolder(destinationFolder.id).first()
            }

            val originFileInfo = vault.fileSystem.findFileInCipherDirectory(parentFolder.id, cipherFileEntity.id)
            val destinationFileInfo =
                vault.fileSystem.findFileInCipherDirectory(destinationFolder.id, cipherFileEntity.id)

            assertAll(
                { assertTrue(destinationFileEntities.any { it.fullName() == fileDisplayName }) },
                { assertTrue(originFileEntities.none { it.fullName() == fileDisplayName }) },
                { assertNotNull(destinationFileInfo) },
                { assertNull(originFileInfo) }
            )
        }

        @ParameterizedTest
        @ArgumentsSource(FileDataProvider::class)
        fun `scheduleRelocation with failing database operation reverts changes`(
            fileDisplayName: String,
            fileLength: Int
        ) {
            // Arrange
            val parentFolder = vault.realm.createRandomCipherFolderEntity()

            val fileToEncrypt = fileSystem.createFile(sharedDirectoryInfo, fileDisplayName)
                .withData(ByteArray(fileLength) { it.toByte() })
            vault.scheduleFileEncryption(SchedulerFake.MetadataFake, listOf { fileToEncrypt }, parentFolder)

            val cipherFileEntity = vault.realm.findCipherFileEntityByName(fileDisplayName).single()
            val destinationFolder = RealmCipherFolderEntity().apply { id = "destination" }

            // Act
            vault.scheduleRelocation(
                metadata = SchedulerFake.MetadataFake,
                destination = destinationFolder,
                files = listOf(cipherFileEntity),
                folders = emptyList()
            )

            // Assert
            val originFileEntities = runBlocking { vault.findCipherFileEntitiesForFolder(parentFolder.id).first() }
            val destinationFileEntities = runBlocking {
                vault.findCipherFileEntitiesForFolder(destinationFolder.id).first()
            }

            val originFileInfo = vault.fileSystem.findFileInCipherDirectory(parentFolder.id, cipherFileEntity.id)
            val destinationFileInfo =
                vault.fileSystem.findFileInCipherDirectory(destinationFolder.id, cipherFileEntity.id)

            assertAll(
                { assertTrue(destinationFileEntities.none { it.fullName() == fileDisplayName }) },
                { assertTrue(originFileEntities.any { it.fullName() == fileDisplayName }) },
                { assertNull(destinationFileInfo) },
                { assertNotNull(originFileInfo) }
            )
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    @Nested
    inner class Folders {

        @Test
        fun `scheduleRelocation without any entities throws IllegalArgumentException`() {
            // Arrange
            val destinationFolder = vault.realm.createRandomCipherFolderEntity()

            // Act & Assert
            assertThrows<IllegalArgumentException> {
                vault.scheduleRelocation(SchedulerFake.MetadataFake, destinationFolder, emptyList(), emptyList())
            }
        }

        @ParameterizedTest
        @ArgumentsSource(DisplayNameProvider::class)
        fun `scheduleRelocation with unmanaged destination throws IllegalArgumentException`(folderDisplayName: String) {
            // Arrange
            val folderToRelocate = vault.realm.createRandomCipherFolderEntity()
            val destinationFolder = RealmCipherFolderEntity().apply {
                id = Uuid.random().toHexString()
                displayName = folderDisplayName
            }

            // Act & Assert
            assertThrows<IllegalArgumentException> {
                vault.scheduleRelocation(
                    metadata = SchedulerFake.MetadataFake,
                    destination = destinationFolder,
                    files = emptyList(),
                    folders = listOf(folderToRelocate)
                )
            }
        }

        @ParameterizedTest
        @ArgumentsSource(DisplayNameProvider::class)
        fun `scheduleRelocation with unmanaged folder to relocate throws IllegalArgumentException`(
            folderDisplayName: String
        ) {
            // Arrange
            val folderToRelocate = RealmCipherFolderEntity().apply {
                id = Uuid.random().toHexString()
                displayName = folderDisplayName
            }
            val destinationFolder = vault.realm.createRandomCipherFolderEntity()

            // Act & Assert
            assertThrows<IllegalArgumentException> {
                vault.scheduleRelocation(
                    metadata = SchedulerFake.MetadataFake,
                    destination = destinationFolder,
                    files = emptyList(),
                    folders = listOf(folderToRelocate)
                )
            }
        }

        @Test
        fun `scheduleRelocation moves empty folder to destination folder`() {
            // Arrange
            val folderToRelocate = vault.realm.createRandomCipherFolderEntity()
            val destinationFolder = vault.realm.createRandomCipherFolderEntity()

            // Act
            vault.scheduleRelocation(
                metadata = SchedulerFake.MetadataFake,
                destination = destinationFolder,
                files = emptyList(),
                folders = listOf(folderToRelocate)
            )

            // Assert
            val subFolders = runBlocking { vault.findCipherFileEntitySubFolders(destinationFolder.id).first() }
            assertTrue(subFolders.any { it.id == folderToRelocate.id })
        }

        @Test
        fun `scheduleRelocation moves empty folder to root folder`() {
            // Arrange
            val parentFolder = vault.realm.createRandomCipherFolderEntity()
            val folderToRelocate = vault.realm.createRandomCipherFolderEntity(parentFolder.id)
            val rootFolder = runBlocking { vault.findCipherFolderEntity("") }

            // Act
            vault.scheduleRelocation(SchedulerFake.MetadataFake, rootFolder, emptyList(), listOf(folderToRelocate))

            // Assert
            val subFolders = runBlocking { vault.findCipherFileEntitySubFolders(rootFolder.id).first() }
            assertTrue(subFolders.any { it.id == parentFolder.id })
        }

        @Test
        fun `scheduleRelocation moves folder with subfolders to destination folder`() {
            // Arrange
            val folderToRelocate = vault.realm.createRandomCipherFolderEntity()
            val subfolder = vault.realm.createRandomCipherFolderEntity(folderToRelocate.id)
            val destinationFolder = vault.realm.createRandomCipherFolderEntity()

            // Act
            vault.scheduleRelocation(
                metadata = SchedulerFake.MetadataFake,
                destination = destinationFolder,
                files = emptyList(),
                folders = listOf(folderToRelocate)
            )

            // Assert
            val destinationSubFolders = runBlocking {
                vault.findCipherFileEntitySubFolders(destinationFolder.id).first()
            }
            val folderToRelocateSubFolders = runBlocking {
                vault.findCipherFileEntitySubFolders(folderToRelocate.id).first()
            }

            assertAll(
                { assertTrue(destinationSubFolders.any { it.id == folderToRelocate.id }) },
                { assertTrue(folderToRelocateSubFolders.any { it.id == subfolder.id }) }
            )
        }

        @ParameterizedTest
        @ArgumentsSource(FileDataProvider::class)
        fun `scheduleRelocation moves folder with file to destination folder`(
            fileDisplayName: String,
            fileLength: Int
        ) {
            // Arrange
            val folderToRelocate = vault.realm.createRandomCipherFolderEntity()
            val destinationFolder = vault.realm.createRandomCipherFolderEntity()

            val fileToEncrypt = fileSystem.createFile(sharedDirectoryInfo, fileDisplayName)
                .withData(ByteArray(fileLength) { it.toByte() })
            vault.scheduleFileEncryption(SchedulerFake.MetadataFake, listOf { fileToEncrypt }, folderToRelocate)

            // Act
            vault.scheduleRelocation(
                metadata = SchedulerFake.MetadataFake,
                destination = destinationFolder,
                files = emptyList(),
                folders = listOf(folderToRelocate)
            )

            // Assert
            val cipherFileEntity = vault.realm.findCipherFileEntityByName(fileDisplayName).single()
            val destinationSubFolders = runBlocking {
                vault.findCipherFileEntitySubFolders(destinationFolder.id).first()
            }
            val folderToRelocateFiles = runBlocking {
                vault.findCipherFileEntitiesForFolder(folderToRelocate.id).first()
            }

            assertAll(
                { assertTrue(destinationSubFolders.any { it.id == folderToRelocate.id }) },
                { assertTrue(folderToRelocateFiles.any { it.id == cipherFileEntity.id }) }
            )
        }
    }
}
