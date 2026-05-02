/*
 * SPDX-FileCopyrightText: 2022 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.domain.vault

import de.lukaspieper.truvark.test.TestContext
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import javax.crypto.AEADBadTagException
import kotlin.uuid.Uuid

class VaultFactoryTests : TestContext(vaultPassword = "z1zrwxrv2foHslr.rrlhxHcXCcwh1p".toByteArray()) {

    @Nested
    inner class CreateVault {

        @Test
        suspend fun `createVault writes a VaultConfig to disk and returns valid vault`() {
            // Act
            val vault = vaultFactory.createVault(vaultDirectoryInfo, vaultPassword)

            // Assert
            val vaultConfig = assertDoesNotThrow { vaultFactory.tryReadVaultConfig(vault.fileSystem.vaultFile)!! }
            assertAll(
                { assertTrue(vaultConfig.id != Uuid.NIL) },
                { assertTrue(vaultConfig.name.isNotBlank()) },
                { assertTrue(vaultConfig.encryptedStreamingAeadKeyset.isNotEmpty()) },
                { assertTrue(vaultConfig.argon2Config.isNotBlank()) },
            )
        }

        @Test
        suspend fun `createVault with empty password throws IllegalArgumentException`() {
            // Act, Assert
            assertThrows<IllegalArgumentException> {
                vaultFactory.createVault(vaultDirectoryInfo, password = ByteArray(0))
            }
        }
    }

    @Nested
    inner class DecryptVault {

        private val validHexEncodedSampleVaultFileContent =
            "0a10f84b2d50364f48ff815960e961207f0612057661756c741a33246172676f6e32696424763d3139246d3d3132382c743d312c703d3124796c584864414d697169336e68423473526e3730425122a201129f013051f6be1d42d4aea83e20c9a58b3dcd00f04b51359eb8e3d7576ce069f0b5fb5fc74cda013650e3f32db0c5f6f8b1e7bfccd9b7c3aaa270a60a75eece02008f353082aaa976ecf3381d21838154f67c06f7d8aff13044365c41787e24034a5684b4e73b7f567a4b40252b8f2967e0a90384911ec4dfb581594e139d5a3c6e729dc8675c7f28dc152261e0bd8204e18e436dc71763789d4324f9c3ef2efc772a9001128d010429f3d7612b36f79779841a07e11b3e0f2cbcf20add63cf82f9925c1e01c3f67eaf4f6cb2391353b4596c43a1c1933efe00bc1abf8fce947a07e1946e127441024158ead7935a4077f11df2eb8b34df85c9c8d7ebfaa62f3a7a8c8cb86fc21646b0e21acb062a7ee61ebcc578e48e2697c31c9dd2fa44e5b7dd42e5f7c4e1fe6a3a919388af92794a0872a929"

        private val invalidHexEncodedSampleVaultFileContent = validHexEncodedSampleVaultFileContent.replace('a', 'b')

        @Test
        suspend fun `decryptVault with valid vault file returns valid vault`() {
            // Arrange
            fileSystem.createFile(vaultDirectoryInfo, VaultConfig.FILENAME)
                .withData(validHexEncodedSampleVaultFileContent.hexToByteArray())

            // Act, Assert
            assertDoesNotThrow {
                vaultFactory.decryptVault(vaultDirectoryInfo, vaultPassword)
            }
        }

        @Test
        suspend fun `decryptVault with invalid vault file throws IllegalStateException`() {
            // Arrange
            fileSystem.createFile(vaultDirectoryInfo, VaultConfig.FILENAME)
                .withData(invalidHexEncodedSampleVaultFileContent.hexToByteArray())

            // Act, Assert
            assertThrows<IllegalStateException> {
                vaultFactory.decryptVault(vaultDirectoryInfo, vaultPassword)
            }
        }

        @Test
        suspend fun `decryptVault returns vault right after creation`() {
            // Act
            val createdVault = vaultFactory.createVault(vaultDirectoryInfo, vaultPassword)
            val decryptedVault = vaultFactory.decryptVault(vaultDirectoryInfo, vaultPassword)

            // Assert
            assertAll(
                { assertEquals(createdVault.id, decryptedVault.id) },
                { assertEquals(createdVault.name, decryptedVault.name) },
            )
        }

        @Test
        suspend fun `decryptVault with tampered vault id throws due to AEAD associated data mismatch`() {
            // Arrange
            val createdVault = vaultFactory.createVault(vaultDirectoryInfo, vaultPassword)

            val tamperedConfig = createdVault.config.copy(id = Uuid.random())
            createdVault.fileSystem.vaultFile.withData(tamperedConfig.toByteArray())

            // Act, Assert
            assertThrows<AEADBadTagException> {
                vaultFactory.decryptVault(vaultDirectoryInfo, vaultPassword)
            }
        }
    }

    @Nested
    inner class ValidatePassword {

        @Test
        suspend fun `validatePassword with valid vault file returns true`() {
            // Arrange
            val vault = vaultFactory.createVault(vaultDirectoryInfo, vaultPassword)

            // Act
            val isValid = vaultFactory.validatePassword(vault, vaultPassword)

            // Assert
            assertTrue(isValid)
        }

        @Test
        suspend fun `validatePassword with valid vault file returns false`() {
            // Arrange
            val vault = vaultFactory.createVault(vaultDirectoryInfo, vaultPassword)

            // Act
            val isValid = vaultFactory.validatePassword(vault, Uuid.random().toByteArray())

            // Assert
            assertFalse(isValid)
        }
    }
}
