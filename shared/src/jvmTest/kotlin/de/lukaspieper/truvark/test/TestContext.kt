/*
 * SPDX-FileCopyrightText: 2025 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.test

import com.google.crypto.tink.RegistryConfiguration
import com.google.crypto.tink.StreamingAead
import com.google.crypto.tink.TinkProtoKeysetFormat
import com.google.crypto.tink.subtle.AesGcmJce
import de.lukaspieper.truvark.crypto.Argon2.Config
import de.lukaspieper.truvark.crypto.JvmArgon2
import de.lukaspieper.truvark.data.io.DirectoryInfo
import de.lukaspieper.truvark.data.io.JavaFileSystem
import de.lukaspieper.truvark.domain.vault.Vault
import de.lukaspieper.truvark.domain.vault.VaultFactory
import de.lukaspieper.truvark.test.doubles.SchedulerFake
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.uuid.Uuid

/**
 * Basis for all test classes using IO, whether they require a vault or not. An Android-like filesystem is created,
 * exposing directories as [File] or [DirectoryInfo]:
 *
 * [TempDir]
 * - [internalDirectory]
 * - [sharedDirectoryInfo]
 * - [vaultDirectoryInfo]
 */
abstract class TestContext(
    protected val vaultName: String = "vault",
    protected val vaultPassword: ByteArray = Uuid.random().toByteArray()
) : IoExtensions, VaultExtensions {

    override val fileSystem = JavaFileSystem()
    protected open val vaultFactory by lazy {
        VaultFactory(
            // Lower memory cost and iterations for faster tests
            argon2 = JvmArgon2(Config(memoryCostInKibibyte = 128, iterations = 1)),
            fileSystem = fileSystem,
            scheduler = SchedulerFake()
        )
    }

    @field:TempDir
    private lateinit var tempDir: File
    private val tempDirectoryInfo by lazy { fileSystem.directoryInfo(tempDir) }

    protected val internalDirectory by lazy { tempDir.resolve("internal").also { it.mkdir() } }
    protected val sharedDirectoryInfo by lazy {
        runBlocking { fileSystem.findOrCreateDirectory(tempDirectoryInfo, "shared") }
    }
    override val vaultDirectoryInfo by lazy {
        runBlocking { fileSystem.findOrCreateDirectory(tempDirectoryInfo, vaultName) }
    }

    private var _vault: Lazy<Vault> = lazy {
        runBlocking { vaultFactory.createVault(vaultDirectoryInfo, vaultPassword) }
    }

    protected val vault: Vault
        get() = _vault.value

    override val vaultStreamingAead: StreamingAead by lazy {
        // Mostly copy-paste from VaultFactory
        val hash = JvmArgon2().hashPassword(vaultPassword, vault.config.argon2Config)
        val passwordBasedKey = AesGcmJce(hash.toRaw())

        val keyset = TinkProtoKeysetFormat.parseEncryptedKeyset(
            vault.config.encryptedStreamingAeadKeyset,
            passwordBasedKey,
            vault.config.id.toByteArray() + VaultFactory.StreamingAeadAssociatedDataSuffix
        )

        keyset.getPrimitive(RegistryConfiguration.get(), StreamingAead::class.java)
    }

    /**
     * Vault caches data like indices in memory. This method clears the cache by reloading the vault from disk.
     */
    protected fun reloadVault() {
        // TODO: Might fail if vault was never accessed before?!
        _vault = lazy { runBlocking { vaultFactory.decryptVault(vaultDirectoryInfo, vaultPassword) } }
    }
}
