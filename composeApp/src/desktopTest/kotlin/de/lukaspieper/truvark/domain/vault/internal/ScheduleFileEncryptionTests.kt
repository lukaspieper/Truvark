/*
 * SPDX-FileCopyrightText: 2023 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.domain.vault.internal

import de.lukaspieper.truvark.domain.entities.RealmCipherFolderEntity
import de.lukaspieper.truvark.domain.vault.Vault
import de.lukaspieper.truvark.test.TestContext
import de.lukaspieper.truvark.test.data.FileDataProvider
import de.lukaspieper.truvark.test.doubles.SchedulerFake
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource
import kotlin.uuid.Uuid

/**
 * Tests for [Vault.scheduleFileEncryption] and the underlying [FileEncryption].
 */
class ScheduleFileEncryptionTests : TestContext() {

    @ParameterizedTest
    @ArgumentsSource(FileDataProvider::class)
    fun `scheduleFileEncryption successfully encrypts file and create entry in Realm database`(
        fileName: String,
        fileLength: Int
    ) {
        // Arrange
        val destinationFolder = vault.realm.createRandomCipherFolderEntity()

        val fileToEncrypt = fileSystem.createFile(sharedDirectoryInfo, fileName)
            .withData(ByteArray(fileLength) { it.toByte() })

        // Act
        vault.scheduleFileEncryption(
            metadata = SchedulerFake.MetadataFake,
            sources = listOf { fileToEncrypt },
            destination = destinationFolder
        )

        // Assert
        val cipherFileEntity = vault.realm.findCipherFileEntityByName(fileName).singleOrNull()
        assertAll(
            { assertNotNull(cipherFileEntity) },
            { assertNotNull(vault.fileSystem.findFileInCipherDirectory(destinationFolder.id, cipherFileEntity!!.id)) },
            { assertTrue(fileSystem.exists(fileToEncrypt)) }
        )
    }

    @ParameterizedTest
    @ArgumentsSource(FileDataProvider::class)
    fun `scheduleFileEncryption succeeds and deletes source file`(fileName: String, fileLength: Int) {
        // Arrange
        val destinationFolder = vault.realm.createRandomCipherFolderEntity()

        val fileToEncrypt = fileSystem.createFile(sharedDirectoryInfo, fileName)
            .withData(ByteArray(fileLength) { it.toByte() })

        // Act
        vault.scheduleFileEncryption(
            metadata = SchedulerFake.MetadataFake,
            sources = listOf { fileToEncrypt },
            destination = destinationFolder,
            deleteSources = true
        )

        // Assert
        val cipherFileEntity = vault.realm.findCipherFileEntityByName(fileName).singleOrNull()
        assertAll(
            { assertNotNull(cipherFileEntity) },
            { assertNotNull(vault.fileSystem.findFileInCipherDirectory(destinationFolder.id, cipherFileEntity!!.id)) },
            { assertFalse(fileSystem.exists(fileToEncrypt)) }
        )
    }

    @ParameterizedTest
    @ArgumentsSource(FileDataProvider::class)
    fun `scheduleFileEncryption with unmanaged CipherFolderEntity throws IllegalArgumentException`(
        fileName: String,
        fileLength: Int
    ) {
        // Arrange
        val destinationFolder = RealmCipherFolderEntity().apply { id = Uuid.random().toHexString() }

        val fileToEncrypt = fileSystem.createFile(sharedDirectoryInfo, fileName)
            .withData(ByteArray(fileLength) { it.toByte() })

        // Act, Assert
        assertThrows<IllegalArgumentException> {
            vault.scheduleFileEncryption(
                metadata = SchedulerFake.MetadataFake,
                sources = listOf { fileToEncrypt },
                destination = destinationFolder
            )
        }
    }
}
