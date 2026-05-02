/*
 * SPDX-FileCopyrightText: 2025 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.test

import com.google.crypto.tink.streamingaead.StreamingAeadConfig
import de.lukaspieper.truvark.constants.FileNames
import de.lukaspieper.truvark.crypto.Argon2.Config
import de.lukaspieper.truvark.crypto.JvmArgon2
import de.lukaspieper.truvark.data.io.DirectoryInfo
import de.lukaspieper.truvark.data.io.FileInfo
import de.lukaspieper.truvark.data.io.JavaFileSystem
import de.lukaspieper.truvark.domain.IdGenerator
import de.lukaspieper.truvark.domain.vault.Vault
import de.lukaspieper.truvark.domain.vault.VaultFactory
import de.lukaspieper.truvark.test.doubles.SchedulerFake
import de.lukaspieper.truvark.test.doubles.ThumbnailProviderFake
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.uuid.Uuid

/**
 * Test basis for all kind of tests using IO, whether they require a vault or not. A Android-like filesystem
 * ([internalDirectory], [sharedDirectoryInfo]) is created on top of [TempDir], exposing directories in form of [File]
 * and [DirectoryInfo].
 */
abstract class TestContext(
    protected val vaultName: String = "vault",
    protected val vaultPassword: ByteArray = Uuid.random().toByteArray(),
    override val fileSystem: JavaFileSystem = JavaFileSystem(),
    protected val vaultFactory: VaultFactory = VaultFactory(
        argon2 = JvmArgon2(
            // Lower memory cost and iterations for faster tests
            defaultConfig = Config(
                memoryCostInKibibyte = 128,
                iterations = 1
            )
        ),
        fileSystem = fileSystem,
        idGenerator = IdGenerator.Default,
        thumbnailProvider = ThumbnailProviderFake(),
        scheduler = SchedulerFake()
    ),
    private val createVault: Boolean = true
) : IoExtensions, RealmExtensions {

    @field:TempDir
    private lateinit var tempDir: File
    private val tempDirectoryInfo by lazy { fileSystem.directoryInfo(tempDir) }

    protected val internalDirectory by lazy { tempDir.resolve("internal").also { it.mkdir() } }
    protected val sharedDirectoryInfo by lazy { fileSystem.findOrCreateDirectory(tempDirectoryInfo, "shared") }
    protected val vaultDirectoryInfo by lazy { fileSystem.findOrCreateDirectory(tempDirectoryInfo, vaultName) }

    protected val vaultCipherFilesDirectoryInfo by lazy {
        fileSystem.findOrCreateDirectory(vaultDirectoryInfo, "files") // Constant is private
    }

    // TODO: Fails without vault initialization, use private constant?
    protected val vaultDecryptionDirectoryInfo
        get() = vault.fileSystem.decryptionRootDirectory

    protected val internalDatabaseFile: File
        get() = internalDirectory.resolve(FileNames.INDEX_REALM)

    protected val vaultFileInfo: FileInfo?
        get() = fileSystem.findFileOrNull(vaultDirectoryInfo, FileNames.VAULT)

    /**
     * [vault] is only initialized if [createVault] is true.
     */
    protected lateinit var vault: Vault
        private set

    @BeforeEach
    fun setUp() {
        StreamingAeadConfig.register()

        if (createVault) {
            vault = vaultFactory.createVault(
                vaultDirectory = vaultDirectoryInfo,
                password = vaultPassword,
                databaseFile = internalDatabaseFile
            )
        }
    }

    @AfterEach
    fun tearDown() {
        if (createVault && !vault.realm.isClosed()) {
            vault.realm.close()
        }
    }
}
