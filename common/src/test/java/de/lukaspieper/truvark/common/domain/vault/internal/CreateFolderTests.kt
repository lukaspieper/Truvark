/*
 * SPDX-FileCopyrightText: 2023 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.common.domain.vault.internal

import de.lukaspieper.truvark.common.domain.entities.RealmCipherFolderEntity
import de.lukaspieper.truvark.common.domain.entities.RootCipherFolderEntity
import de.lukaspieper.truvark.common.domain.vault.Vault
import de.lukaspieper.truvark.common.test.TestContext
import de.lukaspieper.truvark.common.test.data.BlankStringProvider
import de.lukaspieper.truvark.common.test.data.DisplayNameProvider
import de.lukaspieper.truvark.common.test.doubles.IdGeneratorFake
import io.realm.kotlin.ext.query
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Tests for [Vault.createFolder] and the underlying [CipherFolderEntityCreator].
 */
class CreateFolderTests : TestContext() {

    @ParameterizedTest
    @ArgumentsSource(BlankStringProvider::class)
    fun `createFolder with blank name throws IllegalArgumentException`(displayName: String) {
        // Act, Assert
        assertThrows<IllegalArgumentException> {
            runBlocking {
                vault.createFolder(displayName, RootCipherFolderEntity("any display name"))
            }
        }
    }

    @ParameterizedTest
    @ArgumentsSource(DisplayNameProvider::class)
    fun `createFolder successfully creates folder`(displayName: String) {
        // Act
        runBlocking {
            vault.createFolder(displayName, RootCipherFolderEntity("any display name"))
        }

        // Assert
        val cipherFolderEntity = vault.realm.query<RealmCipherFolderEntity>().find().single()
        assertEquals(displayName, cipherFolderEntity.displayName)
    }

    @ParameterizedTest
    @ArgumentsSource(DisplayNameProvider::class)
    fun `createFolder successfully creates sub folder`(displayName: String) {
        // Arrange
        val parentFolder = vault.realm.createRandomCipherFolderEntity()

        // Act
        runBlocking {
            vault.createFolder(displayName, parentFolder)
        }

        // Assert
        val cipherFolderEntities = vault.realm.query<RealmCipherFolderEntity>().find()
        assertAll(
            { assertEquals(2, cipherFolderEntities.size) },
            { assertEquals(displayName, cipherFolderEntities.single { it.id != parentFolder.id }.displayName) },
        )
    }

    @OptIn(ExperimentalUuidApi::class)
    @Test
    fun `createFolder repeats until generated id is unique and creates two CipherFolderEntities`() {
        // Arrange
        val anyId = Uuid.random().toHexString()
        val anyDisplayName = "FolderName"
        val differentId = Uuid.random().toHexString()
        val differentDisplayName = "differentDisplayName"

        val folderCreator = CipherFolderEntityCreator(
            vault.realm,
            IdGeneratorFake(arrayOf(anyId, anyId, differentId))
        )

        // Act
        runBlocking {
            folderCreator.createFolder(anyDisplayName, RootCipherFolderEntity("any display name"))
            folderCreator.createFolder(differentDisplayName, RootCipherFolderEntity("any display name"))
        }

        // Assert
        val cipherFolderEntities = vault.realm.query<RealmCipherFolderEntity>().find()
        assertAll(
            { assertEquals(anyDisplayName, cipherFolderEntities.single { it.id == anyId }.displayName) },
            { assertEquals(differentDisplayName, cipherFolderEntities.single { it.id == differentId }.displayName) },
        )
    }
}
