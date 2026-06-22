/*
 * SPDX-FileCopyrightText: 2022 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.domain.vault

import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.RegistryConfiguration
import com.google.crypto.tink.StreamingAead
import com.google.crypto.tink.TinkProtoKeysetFormat
import com.google.crypto.tink.prf.PredefinedPrfParameters
import com.google.crypto.tink.prf.PrfSet
import com.google.crypto.tink.streamingaead.PredefinedStreamingAeadParameters
import com.google.crypto.tink.subtle.AesGcmJce
import de.lukaspieper.truvark.crypto.Argon2
import de.lukaspieper.truvark.data.io.DirectoryInfo
import de.lukaspieper.truvark.data.io.FileInfo
import de.lukaspieper.truvark.data.io.FileSystem
import de.lukaspieper.truvark.work.Scheduler
import logcat.LogPriority
import logcat.asLog
import logcat.logcat
import kotlin.uuid.Uuid

public class VaultFactory(
    private val argon2: Argon2,
    private val fileSystem: FileSystem,
    private val scheduler: Scheduler
) {
    internal companion object {
        internal val StreamingAeadAssociatedDataSuffix = "StreamingAead".encodeToByteArray()
        private val PrfAssociatedDataSuffix = "Prf".encodeToByteArray()
    }

    public fun tryReadVaultConfig(file: FileInfo): VaultConfig? {
        try {
            require(file.fullName == VaultConfig.FILENAME)

            return fileSystem.openInputStream(file).use { inputStream ->
                VaultConfig.fromByteArray(inputStream.readBytes())
            }
        } catch (e: Exception) {
            logcat(LogPriority.WARN) { e.asLog() }
        }

        return null
    }

    @Throws(Exception::class)
    public suspend fun createVault(vaultDirectory: DirectoryInfo, password: ByteArray): Vault {
        require(password.isNotEmpty()) { "password must not be empty" }

        logcat(LogPriority.INFO) { "Creating new vault..." }
        val vaultConfig = generateVaultConfig(vaultDirectory.name, password)

        // Decrypting the keyset before writing the config for a little bit of extra safety.
        val (streamingAead, prfSet) = decryptKeysets(password, vaultConfig)

        val vaultFile = fileSystem.createFile(vaultDirectory, VaultConfig.FILENAME)
        fileSystem.openOutputStream(vaultFile).use { outputStream ->
            outputStream.write(vaultConfig.toByteArray())
        }
        logcat(LogPriority.INFO) { "VaultConfig written to file. Creation finished." }

        val vaultFileSystem = VaultFileSystem.create(fileSystem, vaultDirectory, vaultFile)
        return Vault(vaultConfig, streamingAead, prfSet, vaultFileSystem, scheduler)
    }

    private fun generateVaultConfig(name: String, password: ByteArray): VaultConfig {
        val vaultId = Uuid.random()
        val hash = argon2.hashPassword(password)
        val passwordBasedKey = AesGcmJce(hash.toRaw())

        val encryptedStreamingAeadKeyset = TinkProtoKeysetFormat.serializeEncryptedKeyset(
            KeysetHandle.generateNew(PredefinedStreamingAeadParameters.AES256_GCM_HKDF_1MB),
            passwordBasedKey,
            vaultId.toByteArray() + StreamingAeadAssociatedDataSuffix
        )

        val encryptedPrfKeyset = TinkProtoKeysetFormat.serializeEncryptedKeyset(
            KeysetHandle.generateNew(PredefinedPrfParameters.HMAC_SHA256_PRF),
            passwordBasedKey,
            vaultId.toByteArray() + PrfAssociatedDataSuffix
        )

        return VaultConfig(
            id = vaultId,
            name = name,
            argon2Config = hash.toEncodedConfigAndSalt(),
            encryptedStreamingAeadKeyset = encryptedStreamingAeadKeyset,
            encryptedPrfKeyset = encryptedPrfKeyset
        )
    }

    public fun validatePassword(vault: Vault, password: ByteArray): Boolean {
        try {
            decryptKeysets(password, vault.config)
            return true
        } catch (e: Exception) {
            logcat(LogPriority.WARN) { e.asLog() }
        }

        return false
    }

    @Throws(Exception::class)
    public suspend fun decryptVault(vaultDirectory: DirectoryInfo, password: ByteArray): Vault {
        val vaultFile = fileSystem.findFileOrNull(vaultDirectory, VaultConfig.FILENAME)
            ?: throw IllegalStateException("Vault file not found in directory.")

        val vaultConfig = tryReadVaultConfig(vaultFile)
        check(vaultConfig != null) { "Could not decode VaultConfig from file." }

        val (streamingAead, prfSet) = decryptKeysets(password, vaultConfig)

        val vaultFileSystem = VaultFileSystem.create(fileSystem, vaultDirectory, vaultFile)
        return Vault(vaultConfig, streamingAead, prfSet, vaultFileSystem, scheduler)
    }

    private fun decryptKeysets(password: ByteArray, vaultConfig: VaultConfig): Pair<StreamingAead, PrfSet> {
        val hash = argon2.hashPassword(password, vaultConfig.argon2Config)
        val passwordBasedKey = AesGcmJce(hash.toRaw())

        val streamingAeadKeyset = TinkProtoKeysetFormat.parseEncryptedKeyset(
            vaultConfig.encryptedStreamingAeadKeyset,
            passwordBasedKey,
            vaultConfig.id.toByteArray() + StreamingAeadAssociatedDataSuffix
        )

        val prfKeyset = TinkProtoKeysetFormat.parseEncryptedKeyset(
            vaultConfig.encryptedPrfKeyset,
            passwordBasedKey,
            vaultConfig.id.toByteArray() + PrfAssociatedDataSuffix
        )

        return Pair(
            streamingAeadKeyset.getPrimitive(RegistryConfiguration.get(), StreamingAead::class.java),
            prfKeyset.getPrimitive(RegistryConfiguration.get(), PrfSet::class.java)
        )
    }
}
