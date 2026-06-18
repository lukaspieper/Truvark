/*
 * SPDX-FileCopyrightText: 2022 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.ui.views.launcher

import android.net.Uri
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.lukaspieper.truvark.KoinModule
import de.lukaspieper.truvark.R
import de.lukaspieper.truvark.data.io.AndroidFileSystem
import de.lukaspieper.truvark.data.io.DirectoryInfo
import de.lukaspieper.truvark.data.preferences.PersistentPreferences
import de.lukaspieper.truvark.domain.crypto.BiometricConfig
import de.lukaspieper.truvark.domain.crypto.BiometricCryptoProvider
import de.lukaspieper.truvark.domain.vault.VaultConfig
import de.lukaspieper.truvark.domain.vault.VaultFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.any
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.onEmpty
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import logcat.LogPriority
import logcat.LogPriority.DEBUG
import logcat.asLog
import logcat.logcat
import java.security.GeneralSecurityException
import javax.crypto.Cipher

public class LauncherViewModel(
    private val preferences: PersistentPreferences,
    private val fileSystem: AndroidFileSystem,
    private val vaultFactory: VaultFactory,
    private val biometricCryptoProvider: BiometricCryptoProvider
) : ViewModel() {
    private var directory: DirectoryInfo? = null
    private var directoryUri: Uri? = null
    private var biometricConfig: BiometricConfig? = null

    public var vaultConfig: VaultConfig? by mutableStateOf(null)
        private set

    public var state: LauncherState by mutableStateOf(LauncherState.PROCESSING)
    public var unlockingErrorText: Int? by mutableStateOf(null)

    public val supportsBiometricUnlocking: Boolean by derivedStateOf {
        unlockingErrorText != R.string.biometric_unlocking_failed && biometricConfig?.vaultId == vaultConfig?.id
    }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            preferences.lastUsedVaultRootUri.first().let { uri ->
                try {
                    val selectedDirectory = fileSystem.directoryInfo(uri)
                    val vaultFile = fileSystem.findFileOrNull(selectedDirectory, VaultConfig.FILENAME)
                    vaultFactory.tryReadVaultConfig(vaultFile!!)!!.let {
                        withContext(Dispatchers.Main) {
                            vaultConfig = it
                            directory = selectedDirectory
                            directoryUri = uri
                            state = LauncherState.NONE
                        }
                    }
                } catch (e: Exception) {
                    logcat(DEBUG) { e.asLog() }

                    withContext(Dispatchers.Main) {
                        state = LauncherState.DIRECTORY_SELECTION
                    }
                }
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            preferences.biometricConfig.collect { biometricConfig = it }
        }
    }

    public fun inspectDirectory(uri: Uri): Job = viewModelScope.launch(Dispatchers.IO) {
        withContext(Dispatchers.Main) {
            state = LauncherState.PROCESSING
            vaultConfig = null
            directory = null
            directoryUri = null
        }

        val selectedDirectory = fileSystem.directoryInfo(uri)

        var hasNoFiles = false
        val vaultFile = fileSystem.listFiles(selectedDirectory)
            .onEmpty { hasNoFiles = true }
            .firstOrNull { it.fullName == VaultConfig.FILENAME }

        if (vaultFile != null) {
            vaultFactory.tryReadVaultConfig(vaultFile)?.let {
                fileSystem.takePersistableUriPermission(uri)
                preferences.saveLastUsedVaultRootUri(uri)

                withContext(Dispatchers.Main) {
                    vaultConfig = it
                    directory = selectedDirectory
                    directoryUri = uri
                    state = LauncherState.NONE
                }
            }
        } else if (hasNoFiles && !fileSystem.listDirectories(selectedDirectory).any { true }) {
            withContext(Dispatchers.Main) {
                directory = selectedDirectory
                directoryUri = uri
                state = LauncherState.VAULT_CREATION
            }
        } else {
            withContext(Dispatchers.Main) {
                state = LauncherState.DIRECTORY_SELECTION
            }
        }
    }

    public fun createVault(password: ByteArray): Job = GlobalScope.launch(Dispatchers.IO) {
        withContext(Dispatchers.Main) {
            state = LauncherState.PROCESSING
        }

        directory!!.let { directory ->
            val vault = vaultFactory.createVault(
                vaultDirectory = directory,
                password = password
            )

            fileSystem.takePersistableUriPermission(directoryUri!!)
            preferences.saveLastUsedVaultRootUri(directoryUri!!)

            KoinModule.createUnlockedVaultScopeOrIgnore(vault)
            withContext(Dispatchers.Main) {
                vaultConfig = vault.config
                state = LauncherState.DONE
            }
        }
    }

    @Throws(Exception::class)
    public fun getCryptoObject(): BiometricPrompt.CryptoObject {
        check(biometricConfig?.iv != null)
        return biometricCryptoProvider.createDecryptingPromptObject(biometricConfig!!.iv)
    }

    public fun unlockWithCipher(cipher: Cipher): Job = viewModelScope.launch(Dispatchers.Default) {
        withContext(Dispatchers.Main) {
            state = LauncherState.PROCESSING
        }

        try {
            val encryptedPassword = biometricConfig!!.accessKey
            val password = cipher.doFinal(encryptedPassword)

            unlockVaultWithPassword(password)
        } catch (e: Exception) {
            logcat(LogPriority.ERROR) { e.asLog() }
            withContext(Dispatchers.Main) {
                disableBiometricUnlockingBecauseOfError()
                state = LauncherState.NONE
            }
        }
    }

    public fun unlockVaultWithPassword(password: ByteArray): Job = viewModelScope.launch(Dispatchers.Default) {
        withContext(Dispatchers.Main) {
            state = LauncherState.PROCESSING
        }

        try {
            val vault = vaultFactory.decryptVault(directory!!, password)

            KoinModule.createUnlockedVaultScopeOrIgnore(vault)
            withContext(Dispatchers.Main) {
                state = LauncherState.DONE
            }
        } catch (exception: Exception) {
            logcat(LogPriority.ERROR) { exception.asLog() }
            withContext(Dispatchers.Main) {
                unlockingErrorText = when (exception) {
                    is GeneralSecurityException -> R.string.incorrect_password
                    else -> R.string.error_unlocking_vault
                }

                state = LauncherState.NONE
            }
        }
    }

    public fun disableBiometricUnlockingBecauseOfError() {
        unlockingErrorText = R.string.biometric_unlocking_failed
    }

    public enum class LauncherState {
        NONE,
        PROCESSING,
        DIRECTORY_SELECTION,
        VAULT_CREATION,
        DONE
    }
}
