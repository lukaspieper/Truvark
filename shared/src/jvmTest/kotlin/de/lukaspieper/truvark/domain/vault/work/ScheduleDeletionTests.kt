/*
 * SPDX-FileCopyrightText: 2023 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.domain.vault.work

import de.lukaspieper.truvark.domain.vault.Vault
import de.lukaspieper.truvark.test.TestContext
import de.lukaspieper.truvark.test.data.AmountProvider
import de.lukaspieper.truvark.test.doubles.SchedulerFake
import de.lukaspieper.truvark.work.DeletingWorkBundle
import kotlinx.coroutines.flow.any
import kotlinx.coroutines.flow.first
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource
import kotlin.uuid.Uuid

/**
 * Tests for [Vault.scheduleDeletion] and the underlying [DeletingWorkBundle].
 */
class ScheduleDeletionTests : TestContext() {

    @Nested
    inner class Files {

        @Test
        suspend fun `scheduleDeletion without physical file deletes index entry`() {
            // Arrange
            val parentFolder = vault.createRandomCipherFolderEntity()
            val cipherFileEntity = vault.createRandomCipherFileEntity(parentFolder)

            // Act
            vault.scheduleDeletion(SchedulerFake.EmptyProperties, setOf(cipherFileEntity), emptySet())

            // Assert
            reloadVault()

            val files = vault.findCipherFileEntitiesForFolder(parentFolder).first()
            assertTrue(files.isEmpty())
        }

        @Test
        suspend fun `scheduleDeletion deletes index entry and physical file`() {
            // Arrange
            val parentFolder = vault.createRandomCipherFolderEntity()
            val cipherFileEntity = vault.createRandomCipherFileEntity(parentFolder)
            val file = vault.fileSystem.createFileInCipherDirectory(parentFolder.id, cipherFileEntity.id)

            // Act
            vault.scheduleDeletion(SchedulerFake.EmptyProperties, setOf(cipherFileEntity), emptySet())

            // Assert
            reloadVault()

            val files = vault.findCipherFileEntitiesForFolder(parentFolder).first()
            assertTrue(files.isEmpty())
            assertFalse(fileSystem.exists(file))
        }
    }

    @Nested
    inner class Folders {

        @ParameterizedTest
        @ArgumentsSource(AmountProvider::class)
        suspend fun `scheduleDeletion without physical directory deletes index entry including subfolders`(
            subfolderAmount: Int
        ) {
            // Arrange
            val parentFolder = vault.createRandomCipherFolderEntity()
            val subfolders = vault.createRandomCipherFolderEntities(subfolderAmount, parentFolder)

            // Act
            vault.scheduleDeletion(SchedulerFake.EmptyProperties, emptySet(), setOf(parentFolder))

            // Assert
            reloadVault()

            (subfolders + parentFolder).forEach { folder ->
                assertThrows<NoSuchElementException> { vault.findCipherFolderEntity(folder.id) }
            }
        }

        @ParameterizedTest
        @ArgumentsSource(AmountProvider::class)
        suspend fun `scheduleDeletion deletes index entry including subfolders and physical directories`(
            subfolderAmount: Int
        ) {
            // Arrange
            val parentFolder = vault.createRandomCipherFolderEntity()
            vault.fileSystem.createFileInCipherDirectory(parentFolder.id, Uuid.random())

            val subfolders = vault.createRandomCipherFolderEntities(subfolderAmount, parentFolder).onEach { subfolder ->
                vault.fileSystem.createFileInCipherDirectory(subfolder.id, Uuid.random())
            }

            // Act
            vault.scheduleDeletion(SchedulerFake.EmptyProperties, emptySet(), setOf(parentFolder))

            // Assert
            reloadVault()

            (subfolders + parentFolder).forEach { folder ->
                assertThrows<NoSuchElementException> { vault.findCipherFolderEntity(folder.id) }
            }

            assertFalse(fileSystem.listDirectories(vault.cipherFilesDirectoryInfo).any { true }) // isEmpty
        }
    }
}
