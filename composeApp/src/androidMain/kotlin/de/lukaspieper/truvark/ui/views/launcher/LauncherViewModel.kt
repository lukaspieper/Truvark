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
import dagger.hilt.android.lifecycle.HiltViewModel
import de.lukaspieper.truvark.R
import de.lukaspieper.truvark.constants.FileNames
import de.lukaspieper.truvark.constants.FixedValues
import de.lukaspieper.truvark.data.database.DatabaseFileSynchronization
import de.lukaspieper.truvark.data.io.AndroidFileSystem
import de.lukaspieper.truvark.data.io.DirectoryInfo
import de.lukaspieper.truvark.data.preferences.PersistentPreferences
import de.lukaspieper.truvark.di.VaultModule
import de.lukaspieper.truvark.domain.IdGenerator
import de.lukaspieper.truvark.domain.crypto.BiometricConfig
import de.lukaspieper.truvark.domain.crypto.BiometricCryptoProvider
import de.lukaspieper.truvark.domain.vault.VaultConfig
import de.lukaspieper.truvark.domain.vault.VaultFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import logcat.LogPriority
import logcat.LogPriority.DEBUG
import logcat.asLog
import logcat.logcat
import java.security.GeneralSecurityException
import javax.crypto.Cipher
import javax.inject.Inject

@HiltViewModel
public class LauncherViewModel @Inject constructor(
    private val preferences: PersistentPreferences,
    private val fileSystem: AndroidFileSystem,
    private val databaseFileSynchronization: DatabaseFileSynchronization,
    private val idGenerator: IdGenerator,
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
                    val vaultFile = fileSystem.findFileOrNull(selectedDirectory, FileNames.VAULT)
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
        val foundFiles = fileSystem.listFiles(selectedDirectory)
        val vaultFile = foundFiles.firstOrNull { it.fullName == FileNames.VAULT }

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
        } else if (foundFiles.isEmpty() && fileSystem.listDirectories(selectedDirectory).isEmpty()) {
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

    public fun createVault(password: String): Job = GlobalScope.launch(Dispatchers.IO) {
        withContext(Dispatchers.Main) {
            state = LauncherState.PROCESSING
        }

        directory!!.let { directory ->
            val vaultId = idGenerator.createStringId(FixedValues.VAULT_ID_LENGTH)

            val vault = vaultFactory.createVault(
                vaultDirectory = directory,
                password = password.toByteArray(),
                databaseFile = fileSystem.appFilesDir().resolve(vaultId).resolve(FileNames.INDEX_REALM),
                vaultId = vaultId
            )

            fileSystem.takePersistableUriPermission(directoryUri!!)
            preferences.saveLastUsedVaultRootUri(directoryUri!!)

            VaultModule.initializeVaultModule(vault)
            withContext(Dispatchers.Main) {
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
            val internalDatabaseFile = fileSystem.appFilesDir().resolve(vaultConfig!!.id).resolve(FileNames.INDEX_REALM)
            databaseFileSynchronization.synchronizeDatabaseFiles(
                vaultDatabaseFile = fileSystem.findOrCreateFile(directory!!, FileNames.INDEX_DATABASE),
                internalDatabaseFile = internalDatabaseFile
            )

            val vault = vaultFactory.decryptVault(
                directory!!,
                password,
                internalDatabaseFile
            )

            VaultModule.initializeVaultModule(vault)
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
