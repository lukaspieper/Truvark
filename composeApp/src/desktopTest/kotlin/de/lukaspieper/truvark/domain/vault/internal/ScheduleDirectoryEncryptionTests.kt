/*
 * SPDX-FileCopyrightText: 2025 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.domain.vault.internal

import de.lukaspieper.truvark.domain.vault.Vault
import de.lukaspieper.truvark.test.TestContext
import de.lukaspieper.truvark.test.data.FileDataProvider
import de.lukaspieper.truvark.test.doubles.SchedulerFake
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource
import kotlin.uuid.Uuid

/**
 * Tests for [Vault.scheduleDirectoryEncryption] and the underlying [FileEncryption].
 */
class ScheduleDirectoryEncryptionTests : TestContext() {

    @Test
    fun `scheduleDirectoryEncryption with empty source creates folder`() {
        // Arrange
        val destinationFolder = vault.realm.createRandomCipherFolderEntity()

        // Act
        runBlocking {
            vault.scheduleDirectoryEncryption(
                metadata = SchedulerFake.MetadataFake,
                source = sharedDirectoryInfo,
                destination = destinationFolder
            )
        }

        // Assert
        val subfolders = runBlocking { vault.findCipherFolderEntitySubfolders(destinationFolder.id).first() }
        assertAll(
            { assertTrue(subfolders.all { folder -> folder.displayName == sharedDirectoryInfo.name }) },
        )
    }

    @ParameterizedTest
    @ArgumentsSource(FileDataProvider::class)
    fun `scheduleDirectoryEncryption with file in source encrypts file in new folder`(
        fileName: String,
        fileLength: Int
    ) {
        // Arrange
        val destinationFolder = vault.realm.createRandomCipherFolderEntity()
        fileSystem.createFile(sharedDirectoryInfo, fileName).withData(ByteArray(fileLength) { it.toByte() })

        // Act
        runBlocking {
            vault.scheduleDirectoryEncryption(
                metadata = SchedulerFake.MetadataFake,
                source = sharedDirectoryInfo,
                destination = destinationFolder
            )
        }

        // Assert
        val cipherFileEntity = vault.realm.findCipherFileEntityByName(fileName).singleOrNull()
        assertAll(
            { assertNotNull(cipherFileEntity) },
            { assertEquals(sharedDirectoryInfo.name, cipherFileEntity!!.folder!!.displayName) },
            {
                assertNotNull(
                    vault.fileSystem.findFileInCipherDirectory(
                        directoryName = cipherFileEntity!!.folder!!.id,
                        fileName = cipherFileEntity.id
                    )
                )
            },
        )
    }

    @ParameterizedTest
    @ArgumentsSource(FileDataProvider::class)
    fun `scheduleDirectoryEncryption with file in subfolder encrypts file`(
        fileName: String,
        fileLength: Int
    ) {
        // Arrange
        val destinationFolder = vault.realm.createRandomCipherFolderEntity()

        val subDirectory = fileSystem.findOrCreateDirectory(sharedDirectoryInfo, Uuid.random().toHexString())
        fileSystem.createFile(subDirectory, fileName).withData(ByteArray(fileLength) { it.toByte() })

        // Act
        runBlocking {
            vault.scheduleDirectoryEncryption(
                metadata = SchedulerFake.MetadataFake,
                source = sharedDirectoryInfo,
                destination = destinationFolder
            )
        }

        // Assert
        val sourceMatchingFolder = runBlocking {
            vault.findCipherFolderEntitySubfolders(destinationFolder.id).first()
                .single { folder -> folder.displayName == sharedDirectoryInfo.name }
        }
        val sourceSubDirectoryMatchingFolder = runBlocking {
            vault.findCipherFolderEntitySubfolders(sourceMatchingFolder.id).first()
                .single { folder -> folder.displayName == subDirectory.name }
        }
        val cipherFileEntity = runBlocking {
            vault.findCipherFileEntitiesForFolder(sourceSubDirectoryMatchingFolder.id).first()
                .singleOrNull { it.fullName() == fileName }
        }

        assertAll(
            { assertNotNull(cipherFileEntity) },
            {
                assertNotNull(
                    vault.fileSystem.findFileInCipherDirectory(
                        directoryName = sourceSubDirectoryMatchingFolder.id,
                        fileName = cipherFileEntity!!.id
                    )
                )
            },
        )
    }
}
