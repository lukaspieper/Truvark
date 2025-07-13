/*
 * SPDX-FileCopyrightText: 2023 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.common.domain.vault.internal

import de.lukaspieper.truvark.common.domain.entities.CipherFolderEntity
import de.lukaspieper.truvark.common.domain.entities.RealmCipherFileEntity
import de.lukaspieper.truvark.common.domain.entities.RealmCipherFolderEntity
import de.lukaspieper.truvark.common.domain.vault.Vault
import de.lukaspieper.truvark.common.test.TestContext
import de.lukaspieper.truvark.common.test.data.AmountProvider
import de.lukaspieper.truvark.common.test.doubles.SchedulerFake
import io.realm.kotlin.ext.query
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertFalse
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
 * Tests for [Vault.scheduleDeletion] and the underlying [FileDeletion].
 */
class ScheduleDeletionTests : TestContext() {

    @Nested
    inner class Files {

        @Test
        fun `scheduleDeletion without physical file deletes realm entry`() {
            // Arrange
            val parentFolder = vault.realm.createRandomCipherFolderEntity()
            val cipherFileEntity = vault.realm.createRandomCipherFileEntity(parentFolder)

            // Act
            vault.scheduleDeletion(SchedulerFake.MetadataFake, listOf(cipherFileEntity), emptyList())

            // Assert
            assertTrue(vault.realm.query<RealmCipherFileEntity>().find().isEmpty())
        }

        @Test
        fun `scheduleDeletion deletes realm entry and physical file`() {
            // Arrange
            val parentFolder = vault.realm.createRandomCipherFolderEntity()
            val cipherFileEntity = vault.realm.createRandomCipherFileEntity(parentFolder)
            val file = vault.fileSystem.createFileInCipherDirectory(parentFolder.id, cipherFileEntity.id)

            // Act
            vault.scheduleDeletion(SchedulerFake.MetadataFake, listOf(cipherFileEntity), emptyList())

            // Assert
            Assertions.assertAll(
                { assertTrue(vault.realm.query<RealmCipherFileEntity>().find().isEmpty()) },
                { assertFalse(fileSystem.exists(file)) }
            )
        }
    }

    @Nested
    inner class Folders {

        @Test
        @OptIn(ExperimentalUuidApi::class)
        fun `scheduleDeletion with invalid folder throws IllegalStateException`() {
            // Arrange
            val folder = RealmCipherFolderEntity().apply { id = Uuid.random().toHexString() }

            // Act, Assert
            assertThrows<IllegalStateException> {
                vault.scheduleDeletion(SchedulerFake.MetadataFake, emptyList(), listOf(folder))
            }
        }

        @ParameterizedTest
        @ArgumentsSource(AmountProvider::class)
        fun `scheduleDeletion without physical directory deletes realm entry including subfolders`(
            subfolderAmount: Int
        ) {
            // Arrange
            val folder = vault.realm.createRandomCipherFolderEntity()
            repeat(subfolderAmount) {
                vault.realm.createRandomCipherFolderEntity(folder.id)
            }

            // Act
            vault.scheduleDeletion(SchedulerFake.MetadataFake, emptyList(), listOf(folder))

            // Assert
            assertTrue(vault.realm.query<RealmCipherFileEntity>().find().isEmpty())
        }

        @ParameterizedTest
        @ArgumentsSource(AmountProvider::class)
        fun `scheduleDeletion deletes realm entry including subfolders and physical directories`(subfolderAmount: Int) {
            // Arrange
            var folder: CipherFolderEntity = vault.realm.createRandomCipherFolderEntity()
            vault.fileSystem.createFileInCipherDirectory(folder.id, "any.file")

            repeat(subfolderAmount) {
                val subfolder = vault.realm.createRandomCipherFolderEntity(folder.id)
                vault.fileSystem.createFileInCipherDirectory(subfolder.id, "any.file")
            }

            // Refresh frozen folder object with updated one
            folder = runBlocking { vault.findCipherFolderEntity(folder.id) }

            // Act
            vault.scheduleDeletion(SchedulerFake.MetadataFake, emptyList(), listOf(folder))

            // Assert
            assertAll(
                { assertTrue(vault.realm.query<RealmCipherFileEntity>().find().isEmpty()) },
                { assertTrue(fileSystem.listDirectories(vaultCipherFilesDirectoryInfo).isEmpty()) }
            )
        }
    }
}
