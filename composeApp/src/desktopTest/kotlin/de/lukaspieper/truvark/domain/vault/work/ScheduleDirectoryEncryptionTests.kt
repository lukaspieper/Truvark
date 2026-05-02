/*
 * SPDX-FileCopyrightText: 2025 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.domain.vault.work

import de.lukaspieper.truvark.domain.vault.Vault
import de.lukaspieper.truvark.test.TestContext
import de.lukaspieper.truvark.test.data.FileDataProvider
import de.lukaspieper.truvark.test.doubles.SchedulerFake
import de.lukaspieper.truvark.work.EncryptingWorkBundle
import kotlinx.coroutines.flow.first
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource
import kotlin.uuid.Uuid

/**
 * Tests for [Vault.scheduleDirectoryEncryption] and the underlying [EncryptingWorkBundle].
 */
class ScheduleDirectoryEncryptionTests : TestContext() {

    @Test
    suspend fun `scheduleDirectoryEncryption with empty source creates folder`() {
        // Arrange
        val destinationFolder = vault.createRandomCipherFolderEntity()

        // Act
        vault.scheduleDirectoryEncryption(
            properties = SchedulerFake.EmptyProperties,
            source = sharedDirectoryInfo,
            destination = destinationFolder
        )

        // Assert
        reloadVault()

        val subfolders = vault.findCipherFolderEntitySubfolders(destinationFolder).first()
        assertTrue(subfolders.all { folder -> folder.displayName == sharedDirectoryInfo.name })
    }

    @ParameterizedTest
    @ArgumentsSource(FileDataProvider::class)
    suspend fun `scheduleDirectoryEncryption with file in source encrypts file in new folder`(
        fileName: String,
        fileLength: Int
    ) {
        // Arrange
        val destinationFolder = vault.createRandomCipherFolderEntity()
        fileSystem.createFile(sharedDirectoryInfo, fileName).withData(ByteArray(fileLength) { it.toByte() })

        // Act
        vault.scheduleDirectoryEncryption(
            properties = SchedulerFake.EmptyProperties,
            source = sharedDirectoryInfo,
            destination = destinationFolder
        )

        // Assert
        reloadVault()

        val sourceMatchingFolder = vault.findCipherFolderEntitySubfolders(destinationFolder).first()
            .single { folder -> folder.displayName == sharedDirectoryInfo.name }

        val cipherFileEntity = vault.findCipherFileEntitiesForFolder(sourceMatchingFolder).first()
            .singleOrNull { it.fullName == fileName }

        assertNotNull(cipherFileEntity)
        assertNotNull(
            vault.fileSystem.findFileInCipherDirectory(
                directoryName = sourceMatchingFolder.id,
                fileName = cipherFileEntity.id
            )
        )
    }

    @ParameterizedTest
    @ArgumentsSource(FileDataProvider::class)
    suspend fun `scheduleDirectoryEncryption with file in subfolder encrypts file`(
        fileName: String,
        fileLength: Int
    ) {
        // Arrange
        val destinationFolder = vault.createRandomCipherFolderEntity()

        val subDirectory = fileSystem.findOrCreateDirectory(sharedDirectoryInfo, Uuid.random().toHexString())
        fileSystem.createFile(subDirectory, fileName).withData(ByteArray(fileLength) { it.toByte() })

        // Act
        vault.scheduleDirectoryEncryption(
            properties = SchedulerFake.EmptyProperties,
            source = sharedDirectoryInfo,
            destination = destinationFolder
        )

        // Assert
        reloadVault()

        val sourceMatchingFolder = vault.findCipherFolderEntitySubfolders(destinationFolder).first()
            .single { folder -> folder.displayName == sharedDirectoryInfo.name }

        val sourceSubDirectoryMatchingFolder = vault.findCipherFolderEntitySubfolders(sourceMatchingFolder).first()
            .single { folder -> folder.displayName == subDirectory.name }

        val cipherFileEntity = vault.findCipherFileEntitiesForFolder(sourceSubDirectoryMatchingFolder).first()
            .singleOrNull { it.fullName == fileName }

        assertNotNull(cipherFileEntity)
        assertNotNull(
            vault.fileSystem.findFileInCipherDirectory(
                directoryName = sourceSubDirectoryMatchingFolder.id,
                fileName = cipherFileEntity.id
            )
        )
    }
}
