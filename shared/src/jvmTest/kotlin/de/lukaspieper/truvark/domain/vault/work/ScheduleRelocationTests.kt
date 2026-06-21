/*
 * SPDX-FileCopyrightText: 2024 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.domain.vault.work

import de.lukaspieper.truvark.domain.vault.Vault
import de.lukaspieper.truvark.test.TestContext
import de.lukaspieper.truvark.test.data.FileDataProvider
import de.lukaspieper.truvark.test.doubles.SchedulerFake
import de.lukaspieper.truvark.work.RelocatingWorkBundle
import kotlinx.coroutines.flow.first
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertNull
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource

/**
 * Tests for [Vault.scheduleRelocation] and the underlying [RelocatingWorkBundle].
 */
class ScheduleRelocationTests : TestContext() {

    @Nested
    inner class Files {

        @ParameterizedTest
        @ArgumentsSource(FileDataProvider::class)
        suspend fun `scheduleRelocation with single file is successful`(fileDisplayName: String, fileLength: Int) {
            // Arrange
            val parentFolder = vault.createRandomCipherFolderEntity()

            val fileToEncrypt = fileSystem.createFile(sharedDirectoryInfo, fileDisplayName)
                .withData(ByteArray(fileLength) { it.toByte() })
            vault.scheduleFileEncryption(SchedulerFake.EmptyProperties, listOf { fileToEncrypt }, parentFolder)

            val cipherFileEntity = vault.findCipherFileEntitiesForFolder(parentFolder).first().single()
            val destinationFolder = vault.createRandomCipherFolderEntity()

            reloadVault()

            // Act
            vault.scheduleRelocation(
                properties = SchedulerFake.EmptyProperties,
                destination = destinationFolder,
                files = setOf(cipherFileEntity),
                folders = emptySet()
            )

            // Assert
            val originFileEntities = vault.findCipherFileEntitiesForFolder(parentFolder).first()
            val destinationFileEntities = vault.findCipherFileEntitiesForFolder(destinationFolder).first()

            val originFileInfo = vault.fileSystem.findFileInCipherDirectory(parentFolder.id, cipherFileEntity.id)
            val destinationFileInfo =
                vault.fileSystem.findFileInCipherDirectory(destinationFolder.id, cipherFileEntity.id)

            assertTrue(destinationFileEntities.any { it.fullName == fileDisplayName })
            assertTrue(originFileEntities.none { it.fullName == fileDisplayName })
            assertNotNull(destinationFileInfo)
            assertNull(originFileInfo)
        }
    }

    @Nested
    inner class Folders {

        @Test
        suspend fun `scheduleRelocation without any entities throws IllegalArgumentException`() {
            // Arrange
            val destinationFolder = vault.createRandomCipherFolderEntity()

            // Act & Assert
            assertThrows<IllegalArgumentException> {
                vault.scheduleRelocation(SchedulerFake.EmptyProperties, destinationFolder, emptySet(), emptySet())
            }
        }

        @Test
        suspend fun `scheduleRelocation moves empty folder to destination folder`() {
            // Arrange
            val folderToRelocate = vault.createRandomCipherFolderEntity()
            val destinationFolder = vault.createRandomCipherFolderEntity()

            // Act
            vault.scheduleRelocation(
                properties = SchedulerFake.EmptyProperties,
                destination = destinationFolder,
                files = emptySet(),
                folders = setOf(folderToRelocate)
            )

            // Assert
            val updatedFolderToRelocate = vault.findCipherFolderEntity(folderToRelocate.id)
            val destinationSubfolders = vault.findCipherFolderEntitySubfolders(destinationFolder).first()

            assertEquals(
                folderToRelocate.copy(parentFolderId = updatedFolderToRelocate.parentFolderId),
                updatedFolderToRelocate
            )
            assertTrue(destinationSubfolders.contains(updatedFolderToRelocate))
        }

        @Test
        suspend fun `scheduleRelocation moves empty folder to root folder`() {
            // Arrange
            val parentFolder = vault.createRandomCipherFolderEntity()
            val folderToRelocate = vault.createRandomCipherFolderEntity(parentFolder)

            // Act
            vault.scheduleRelocation(
                SchedulerFake.EmptyProperties,
                vault.rootFolder,
                emptySet(),
                setOf(folderToRelocate)
            )

            // Assert
            val updatedFolderToRelocate = vault.findCipherFolderEntity(folderToRelocate.id)
            val destinationSubfolders = vault.findCipherFolderEntitySubfolders(vault.rootFolder).first()

            assertEquals(
                folderToRelocate.copy(parentFolderId = updatedFolderToRelocate.parentFolderId),
                updatedFolderToRelocate
            )
            assertTrue(destinationSubfolders.contains(updatedFolderToRelocate))
        }

        @Test
        suspend fun `scheduleRelocation moves folder with subfolders to destination folder`() {
            // Arrange
            val folderToRelocate = vault.createRandomCipherFolderEntity()
            val subfolder = vault.createRandomCipherFolderEntity(folderToRelocate)
            val destinationFolder = vault.createRandomCipherFolderEntity()

            // Act
            vault.scheduleRelocation(
                properties = SchedulerFake.EmptyProperties,
                destination = destinationFolder,
                files = emptySet(),
                folders = setOf(folderToRelocate)
            )

            // Assert
            val updatedFolderToRelocate = vault.findCipherFolderEntity(folderToRelocate.id)
            val destinationSubfolders = vault.findCipherFolderEntitySubfolders(destinationFolder).first()
            val folderToRelocateSubfolders = vault.findCipherFolderEntitySubfolders(folderToRelocate).first()

            assertEquals(
                folderToRelocate.copy(parentFolderId = updatedFolderToRelocate.parentFolderId),
                updatedFolderToRelocate
            )
            assertTrue(destinationSubfolders.contains(updatedFolderToRelocate))
            assertTrue(folderToRelocateSubfolders.contains(subfolder))
        }

        @ParameterizedTest
        @ArgumentsSource(FileDataProvider::class)
        suspend fun `scheduleRelocation moves folder with file to destination folder`(
            fileDisplayName: String,
            fileLength: Int
        ) {
            // Arrange
            val folderToRelocate = vault.createRandomCipherFolderEntity()
            val destinationFolder = vault.createRandomCipherFolderEntity()

            val fileToEncrypt = fileSystem.createFile(sharedDirectoryInfo, fileDisplayName)
                .withData(ByteArray(fileLength) { it.toByte() })
            vault.scheduleFileEncryption(SchedulerFake.EmptyProperties, listOf { fileToEncrypt }, folderToRelocate)

            // Act
            vault.scheduleRelocation(
                properties = SchedulerFake.EmptyProperties,
                destination = destinationFolder,
                files = emptySet(),
                folders = setOf(folderToRelocate)
            )

            // Assert
            val updatedFolderToRelocate = vault.findCipherFolderEntity(folderToRelocate.id)
            val destinationSubfolders = vault.findCipherFolderEntitySubfolders(destinationFolder).first()
            val cipherFileEntity = vault.findCipherFileEntitiesForFolder(folderToRelocate).first().single()

            assertEquals(
                folderToRelocate.copy(parentFolderId = updatedFolderToRelocate.parentFolderId),
                updatedFolderToRelocate
            )
            assertTrue { destinationSubfolders.contains(updatedFolderToRelocate) }
            assertEquals(folderToRelocate.id, cipherFileEntity.folderId)
        }
    }
}
