/*
 * SPDX-FileCopyrightText: 2023 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.domain.vault.work

import de.lukaspieper.truvark.domain.entities.CipherFileEntity
import de.lukaspieper.truvark.domain.vault.Vault
import de.lukaspieper.truvark.test.TestContext
import de.lukaspieper.truvark.test.data.FileDataProvider
import de.lukaspieper.truvark.test.doubles.SchedulerFake
import de.lukaspieper.truvark.work.EncryptingWorkBundle
import kotlinx.coroutines.flow.first
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource
import java.security.MessageDigest

/**
 * Tests for [Vault.scheduleFileEncryption] and the underlying [EncryptingWorkBundle].
 */
class ScheduleFileEncryptionTests : TestContext() {

    @ParameterizedTest
    @ArgumentsSource(FileDataProvider::class)
    suspend fun `scheduleFileEncryption successfully encrypts file and creates the file index`(
        fileName: String,
        fileLength: Int
    ) {
        // Arrange
        val destinationFolder = vault.createRandomCipherFolderEntity()

        val fileData = ByteArray(fileLength) { it.toByte() }
        val fileToEncrypt = fileSystem.createFile(sharedDirectoryInfo, fileName).withData(fileData)

        // Act
        vault.scheduleFileEncryption(
            properties = SchedulerFake.EmptyProperties,
            sources = listOf { fileToEncrypt },
            destination = destinationFolder
        )

        // Assert
        reloadVault()

        val cipherFileEntity = vault.findCipherFileEntitiesForFolder(destinationFolder)
            .first()
            .singleOrNull { it.fullName == fileName }
        assertNotNull(cipherFileEntity)

        val expectedCipherFileEntity = CipherFileEntity(
            id = cipherFileEntity.id,
            fullName = fileName,
            mimeType = "application/octet-stream",
            fileSize = fileLength.toLong(),
            mediaDuration = null,
            creationTimestamp = cipherFileEntity.creationTimestamp,
            folderId = destinationFolder.id
        )
        expectedCipherFileEntity.sha256Digest = fileData.sha256()
        assertEquals(expectedCipherFileEntity, cipherFileEntity)

        assertNotNull(vault.fileSystem.findFileInCipherDirectory(destinationFolder.id, cipherFileEntity.id))
        assertTrue(fileSystem.exists(fileToEncrypt))
    }

    @ParameterizedTest
    @ArgumentsSource(FileDataProvider::class)
    suspend fun `scheduleFileEncryption succeeds and deletes source file`(fileName: String, fileLength: Int) {
        // Arrange
        val destinationFolder = vault.createRandomCipherFolderEntity()

        val fileData = ByteArray(fileLength) { it.toByte() }
        val fileToEncrypt = fileSystem.createFile(sharedDirectoryInfo, fileName).withData(fileData)

        // Act
        vault.scheduleFileEncryption(
            properties = SchedulerFake.EmptyProperties,
            sources = listOf { fileToEncrypt },
            destination = destinationFolder,
            deleteSources = true
        )

        // Assert
        reloadVault()

        val cipherFileEntity = vault.findCipherFileEntitiesForFolder(destinationFolder)
            .first()
            .singleOrNull { it.fullName == fileName }
        assertNotNull(cipherFileEntity)

        val expectedCipherFileEntity = CipherFileEntity(
            id = cipherFileEntity.id,
            fullName = fileName,
            mimeType = "application/octet-stream",
            fileSize = fileLength.toLong(),
            mediaDuration = null,
            creationTimestamp = cipherFileEntity.creationTimestamp,
            folderId = destinationFolder.id
        )
        expectedCipherFileEntity.sha256Digest = fileData.sha256()
        assertEquals(expectedCipherFileEntity, cipherFileEntity)

        assertNotNull(vault.fileSystem.findFileInCipherDirectory(destinationFolder.id, cipherFileEntity.id))
        assertFalse(fileSystem.exists(fileToEncrypt))
    }

    fun ByteArray.sha256(): ByteArray {
        val messageDigest = MessageDigest.getInstance("SHA-256")
        return messageDigest.digest(this)
    }
}
