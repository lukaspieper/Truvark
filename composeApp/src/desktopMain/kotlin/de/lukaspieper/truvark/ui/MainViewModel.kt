/*
 * SPDX-FileCopyrightText: 2023 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import de.lukaspieper.truvark.data.io.DirectoryInfo
import de.lukaspieper.truvark.data.io.JavaFileSystem
import de.lukaspieper.truvark.domain.vault.Vault
import de.lukaspieper.truvark.domain.vault.VaultConfig
import de.lukaspieper.truvark.domain.vault.VaultFactory
import kotlinx.coroutines.flow.any
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.onEmpty
import kotlinx.coroutines.runBlocking
import logcat.LogPriority
import logcat.asLog
import logcat.logcat
import java.io.File

public class MainViewModel(
    private val fileSystem: JavaFileSystem = JavaFileSystem(),
    private val vaultFactory: VaultFactory
) {
    public var directory: DirectoryInfo? by mutableStateOf(null)
        private set

    public var vaultConfig: VaultConfig? by mutableStateOf(null)
        private set

    public var vault: Vault? by mutableStateOf(null)
        private set

    public fun inspectDirectory(file: File) {
        vault = null
        this.vaultConfig = null
        this.directory = null

        val selectedDirectory = fileSystem.directoryInfo(file)

        var hasNoFiles = false
        val vaultFile = runBlocking {
            fileSystem.listFiles(selectedDirectory)
                .onEmpty { hasNoFiles = true }
                .firstOrNull { it.fullName == VaultConfig.FILENAME }
        }

        val hasNoDirectories = runBlocking {
            !fileSystem.listDirectories(selectedDirectory).any { true }
        }

        if (vaultFile != null) {
            val vaultConfig = vaultFactory.tryReadVaultConfig(vaultFile)

            if (vaultConfig != null) {
                this.vaultConfig = vaultConfig
                this.directory = selectedDirectory
            }
        } else if (hasNoFiles && hasNoDirectories) {
            this.directory = selectedDirectory
        }
    }

    public fun createVault(password: String) {
        directory!!.let { vaultDirectory ->
            vault = runBlocking { vaultFactory.createVault(vaultDirectory, password.toByteArray()) }
        }
    }

    public fun unlockVault(password: String) {
        directory!!.let { vaultDirectory ->
            try {
                vault = runBlocking { vaultFactory.decryptVault(vaultDirectory, password.toByteArray()) }
            } catch (e: Exception) {
                // TODO: Show error to user
                logcat(LogPriority.WARN) { "Failed to unlock vault: ${e.asLog()}" }
            }
        }
    }
}
