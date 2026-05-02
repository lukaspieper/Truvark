/*
 * SPDX-FileCopyrightText: 2022 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.domain.vault

import de.lukaspieper.truvark.constants.FileNames
import de.lukaspieper.truvark.test.TestContext
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import java.security.GeneralSecurityException
import kotlin.uuid.Uuid

class VaultFactoryTests : TestContext(
    vaultPassword = "z1zrwxrv2foHslr.rrlhxHcXCcwh1p".toByteArray(),
    createVault = false
) {
    @Nested
    inner class CreateVault {

        @Test
        fun `createVault writes a VaultConfig to disk and returns valid vault`() {
            // Act
            val vault = vaultFactory.createVault(
                vaultDirectory = vaultDirectoryInfo,
                password = vaultPassword,
                databaseFile = internalDatabaseFile
            )

            // Assert
            val vaultConfig = assertDoesNotThrow { vaultFactory.tryReadVaultConfig(vaultFileInfo!!)!! }
            assertAll(
                { assertTrue(vaultConfig.encryptedKeyset.isNotBlank()) },
                { assertTrue(vaultConfig.id.isNotBlank()) },
                { assertTrue(vaultConfig.displayName.isNotBlank()) },
                { assertTrue(vaultConfig.encryptedDatabaseKey.isNotEmpty()) },
            )

            vault.realm.close()
        }

        @Test
        fun `createVault with empty password throws IllegalArgumentException`() {
            // Act, Assert
            assertThrows<IllegalArgumentException> {
                vaultFactory.createVault(
                    vaultDirectory = vaultDirectoryInfo,
                    password = ByteArray(0),
                    databaseFile = internalDatabaseFile
                )
            }
        }

        @Test
        fun `createVault with invalid databaseFile throws IllegalArgumentException`() {
            // Act, Assert
            assertThrows<IllegalArgumentException> {
                vaultFactory.createVault(
                    vaultDirectory = vaultDirectoryInfo,
                    password = vaultPassword,
                    databaseFile = internalDirectory // is a directory, not a file. Therefore it is invalid.
                )
            }
        }
    }

    @Nested
    inner class DecryptVault {

        // `${'$'}` == `$`
        private val validSampleVaultFileContent =
            """
            {
                "DisplayName": "junit",
                "EncryptedKeyset": "Ep4B6cPiN+GNKKuPh75MJHUGxwHvo771p3cSeUITPmultVsYJJ/nUJgdXsz8Ph764AXKGFdSkFvHihFXPo1EN5CcNNMh4utvmNzx3aEL0a5QsSNTPLNTUSWkBnb1ITga1kF7DUnKc4MCYidVAzXn5vZpT1zZ6VfODJHISnSMXrDhUAiIMiwQefwkIHoDYuHdnelpZEq6fXdU9knq5hDMyOc${'$'}argon2id${'$'}v=19${'$'}m=65536,t=3,p=1${'$'}u/Ky8+ZqrCm++NnJ/1SstQ",
                "Id": "VwNaOs",
                "EncryptedDatabaseKey": [40, -55, 37, 66, 127, -30, 36, -31, 91, 8, 50, -50, -117, 28, -90, -109, -98, -115, 69, -4, -62, -45, 28, 30, -127, -34, 60, -40, -56, -125, -7, -5, -70, 27, -107, -74, 64, 76, 82, -12, -45, -124, -54, 81, -107, -68, 82, 52, 45, -25, 110, -88, -104, 120, -66, 43, -126, 94, 90, 51, -92, 10, 116, 59, 98, -40, -75, 94, -10, 9, -101, -121, -90, 75, 55, 96, -77, 104, -97, -94, 50, 105, 2, -128, 118, -68, 36, -14, 0, 118, 7, 30, -58, -35, -82, 31, -35, -113, -56, 26, 23, -114, 36, 84, 76, 22, 60, -49, -53, -76, 12, 121, -11, 100, 61, 65, -49, 109, -23, 4]
            }
        """.toByteArray()

        // encryptedKeyset is modified, compare with above one
        private val invalidSampleVaultFileContent =
            """
            {
                "DisplayName": "junit",
                "EncryptedKeyset": "Up4B6cPiN+GNKKuPh75MJHUGxwHvo771p3cSeUITPmultVsYJJ/nUJgdXsz8Ph764AXKGFdSkFvHihFXPo1EN5CcNNMh4utvmNzx3aEL0a5QsSNTPLNTUSWkBnb1ITga1kF7DUnKc4MCYidVAzXn5vZpT1zZ6VfODJHISnSMXrDhUAiIMiwQefwkIHoDYuHdnelpZEq6fXdU9knq5hDMyOc${'$'}argon2id${'$'}v=19${'$'}m=65536,t=3,p=1${'$'}u/Ky8+ZqrCm++NnJ/1SstQ",
                "Id": "VwNaOs"
            }
        """.toByteArray()

        @Test
        fun `decryptVault with valid vault file and invalid database file throws IllegalStateException`() {
            // Arrange
            fileSystem.createFile(vaultDirectoryInfo, FileNames.VAULT).withData(validSampleVaultFileContent)
            internalDatabaseFile.writeBytes(ByteArray(16))

            // Act, Assert
            assertThrows<IllegalStateException> {
                vaultFactory.decryptVault(
                    vaultDirectory = vaultDirectoryInfo,
                    password = vaultPassword,
                    databaseFile = internalDatabaseFile
                )
            }
        }

        @Test
        fun `decryptVault with invalid vault file throws GeneralSecurityException`() {
            // Arrange
            fileSystem.createFile(vaultDirectoryInfo, FileNames.VAULT).withData(invalidSampleVaultFileContent)
            internalDatabaseFile.createNewFile() // database file must exist to pass parameter validation

            // Act, Assert
            assertThrows<GeneralSecurityException> {
                vaultFactory.decryptVault(
                    vaultDirectory = vaultDirectoryInfo,
                    password = vaultPassword,
                    databaseFile = internalDatabaseFile
                )
            }
        }

        @Test
        fun `decryptVault returns vault right after creation`() {
            // Act
            val createdVault = vaultFactory.createVault(
                vaultDirectory = vaultDirectoryInfo,
                password = vaultPassword,
                databaseFile = internalDatabaseFile
            )
            createdVault.realm.close()

            val decryptedVault = vaultFactory.decryptVault(
                vaultDirectory = vaultDirectoryInfo,
                password = vaultPassword,
                databaseFile = internalDatabaseFile
            )
            decryptedVault.realm.close()

            // Assert
            assertAll(
                { assertEquals(createdVault.id, decryptedVault.id) },
                { assertEquals(createdVault.displayName, decryptedVault.displayName) },
                { assertNotNull(createdVault.realm) },
                { assertNotNull(decryptedVault.realm) },
            )
        }
    }

    @Nested
    inner class ValidatePassword {

        @Test
        fun `validatePassword with valid vault file returns true`() {
            // Arrange
            val vault = vaultFactory.createVault(
                vaultDirectory = vaultDirectoryInfo,
                password = vaultPassword,
                databaseFile = internalDatabaseFile
            )

            // Act
            val isValid = vaultFactory.validatePassword(vault, vaultPassword)

            // Assert
            assertTrue(isValid)
        }

        @Test
        fun `validatePassword with valid vault file returns false`() {
            // Arrange
            val vault = vaultFactory.createVault(
                vaultDirectory = vaultDirectoryInfo,
                password = vaultPassword,
                databaseFile = internalDatabaseFile
            )

            // Act
            val isValid = vaultFactory.validatePassword(vault, Uuid.random().toByteArray())

            // Assert
            assertFalse(isValid)
        }
    }
}
