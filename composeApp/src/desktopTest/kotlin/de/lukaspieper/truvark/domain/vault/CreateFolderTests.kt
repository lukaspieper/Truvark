/*
 * SPDX-FileCopyrightText: 2023 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.domain.vault

import de.lukaspieper.truvark.test.TestContext
import de.lukaspieper.truvark.test.data.BlankStringProvider
import de.lukaspieper.truvark.test.data.DisplayNameProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource
import org.junit.jupiter.params.provider.ValueSource

/**
 * Tests for [Vault.createFolder].
 */
class CreateFolderTests : TestContext() {

    @ParameterizedTest
    @ArgumentsSource(BlankStringProvider::class)
    suspend fun `createFolder with blank name throws IllegalArgumentException`(displayName: String) {
        // Act, Assert
        assertThrows<IllegalArgumentException> {
            vault.createFolder(displayName, vault.rootFolder)
        }
    }

    @ParameterizedTest
    @ArgumentsSource(DisplayNameProvider::class)
    suspend fun `createFolder successfully creates folder`(displayName: String) {
        // Act
        vault.createFolder(displayName, vault.rootFolder)

        // Assert
        reloadVault()

        val cipherFolderEntities = vault.findCipherFolderEntitySubfolders(vault.rootFolder).first()
        assertNotNull(cipherFolderEntities.singleOrNull { folder -> folder.displayName == displayName })
    }

    @ParameterizedTest
    @ArgumentsSource(DisplayNameProvider::class)
    suspend fun `createFolder successfully creates sub folder`(displayName: String) {
        // Arrange
        val parentFolder = vault.createRandomCipherFolderEntity()

        // Act
        vault.createFolder(displayName, parentFolder)

        // Assert
        reloadVault()

        val cipherFolderEntities = vault.findCipherFolderEntitySubfolders(parentFolder).first()
        assertEquals(displayName, cipherFolderEntities.single { it.id != parentFolder.id }.displayName)
    }

    @ParameterizedTest
    @ValueSource(ints = [25])
    suspend fun `createFolder is thread-safe`(amount: Int) = withContext(Dispatchers.Default) {
        // Act
        (0..<amount)
            .map { index -> async { vault.createFolder(index.toPaddedString(), vault.rootFolder) } }
            .awaitAll()

        // Assert
        val expectedDisplayNames = (0..<amount).map { index -> index.toPaddedString() }.sorted()

        reloadVault()
        val folders = vault.findCipherFolderEntitySubfolders(vault.rootFolder).first()
        val displayNames = folders.map { it.displayName }.sorted()

        assertEquals(expectedDisplayNames, displayNames)
    }

    @ParameterizedTest
    @ValueSource(ints = [25])
    suspend fun `createFolder and renameFolder are thread-safe`(amount: Int) = withContext(Dispatchers.Default) {
        // Act
        (0..<amount)
            .map { index -> async { vault.createFolder(index.toPaddedString(), vault.rootFolder) } }
            .map { deferred ->
                async {
                    val folder = deferred.await()
                    vault.renameFolder(folder, "${folder.displayName}!")
                    vault.findCipherFolderEntity(folder.id)
                }
            }
            .awaitAll()

        // Assert
        val expectedDisplayNames = (0..<amount).map { index -> "${index.toPaddedString()}!" }.sorted()

        reloadVault()
        val folders = vault.findCipherFolderEntitySubfolders(vault.rootFolder).first()
        val displayNames = folders.map { it.displayName }.sorted()

        assertEquals(expectedDisplayNames, displayNames)
    }
}
