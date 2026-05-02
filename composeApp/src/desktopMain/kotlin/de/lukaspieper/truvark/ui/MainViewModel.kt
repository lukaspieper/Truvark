/*
 * SPDX-FileCopyrightText: 2023 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import de.lukaspieper.truvark.constants.FileNames
import de.lukaspieper.truvark.data.io.DirectoryInfo
import de.lukaspieper.truvark.data.io.JavaFileSystem
import de.lukaspieper.truvark.domain.vault.Vault
import de.lukaspieper.truvark.domain.vault.VaultConfig
import de.lukaspieper.truvark.domain.vault.VaultFactory
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

        val directory = fileSystem.directoryInfo(file)
        val foundFiles = fileSystem.listFiles(directory)
        val vaultFile = foundFiles.firstOrNull { it.fullName == FileNames.VAULT }

        if (vaultFile != null) {
            val vaultConfig = vaultFactory.tryReadVaultConfig(vaultFile)
            if (vaultConfig != null) {
                this.vaultConfig = vaultConfig
                this.directory = directory
            }
        } else if (foundFiles.isEmpty() && fileSystem.listDirectories(directory).isEmpty()) {
            this.directory = directory
        }
    }

    public fun createVault(password: String) {
        directory!!.let {
            vault = vaultFactory.createVault(
                vaultDirectory = it,
                password = password.toByteArray(),
                databaseFile = (it.uri as File).resolve(FileNames.INDEX_DATABASE)
            )
        }
    }

    public fun unlockVault(password: String) {
        directory!!.let {
            vault = vaultFactory.decryptVault(
                vaultDirectory = it,
                password = password.toByteArray(),
                databaseFile = (it.uri as File).resolve(FileNames.INDEX_DATABASE)
            )
        }
    }
}
