/*
 * SPDX-FileCopyrightText: 2022 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.ui.views.launcher

import android.net.Uri
import androidx.annotation.StringRes
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.lukaspieper.truvark.KoinModule
import de.lukaspieper.truvark.R
import de.lukaspieper.truvark.data.io.AndroidFileSystem
import de.lukaspieper.truvark.data.io.DirectoryInfo
import de.lukaspieper.truvark.data.preferences.PersistentPreferences
import de.lukaspieper.truvark.data.preferences.models.BiometricConfig
import de.lukaspieper.truvark.domain.crypto.BiometricCryptoProvider
import de.lukaspieper.truvark.domain.vault.VaultConfig
import de.lukaspieper.truvark.domain.vault.VaultFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.any
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEmpty
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import logcat.LogPriority
import logcat.LogPriority.DEBUG
import logcat.asLog
import logcat.logcat
import java.security.GeneralSecurityException
import javax.crypto.Cipher
import kotlin.uuid.Uuid

public class LauncherViewModel(
    private val preferences: PersistentPreferences,
    private val fileSystem: AndroidFileSystem,
    private val vaultFactory: VaultFactory,
    private val biometricCryptoProvider: BiometricCryptoProvider
) : ViewModel() {
    private val unlockingError = MutableStateFlow<Pair<Uuid, Int>?>(null)

    private val recentlyUsedVaults = preferences.recentVaultRootUris
        .map { uris ->
            uris.mapNotNull { uri ->
                try {
                    val selectedDirectory = fileSystem.directoryInfo(uri)
                    val vaultFile = fileSystem.findFileOrNull(selectedDirectory, VaultConfig.FILENAME)
                    val vaultConfig = vaultFactory.tryReadVaultConfig(vaultFile!!)

                    RecentVaultInfo(directory = selectedDirectory, config = vaultConfig!!)
                } catch (exception: Exception) {
                    logcat(DEBUG) { exception.asLog() }
                    null
                }
            }
        }
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    public var vaultEntries: StateFlow<List<RecentVaultInfo>> = combine(
        recentlyUsedVaults.mapNotNull { it },
        preferences.biometricConfig,
        unlockingError
    ) { vaults, biometricConfig, unlockingErrorPair ->
        vaults.map { vault ->
            vault.copy(
                biometricConfig = biometricConfig?.takeIf { it.vaultId == vault.config.id },
                unlockingErrorText = unlockingErrorPair?.takeIf { it.first == vault.config.id }?.second
            )
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    public val state: MutableStateFlow<LauncherState> = MutableStateFlow<LauncherState>(LauncherState.Processing)

    public val isAnyDebuggingSettingEnabled: Flow<Boolean> = preferences.isAnyDebuggingSettingEnabled

    init {
        viewModelScope.launch {
            recentlyUsedVaults.first { it != null }
            state.value = LauncherState.Ready()
        }
    }

    public fun inspectDirectory(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            state.value = LauncherState.Processing

            val selectedDirectory = fileSystem.directoryInfo(uri)

            var hasNoFiles = false
            val vaultFile = fileSystem.listFiles(selectedDirectory)
                .onEmpty { hasNoFiles = true }
                .firstOrNull { it.fullName == VaultConfig.FILENAME }

            if (vaultFile != null) {
                vaultFactory.tryReadVaultConfig(vaultFile)?.let { vaultConfig ->
                    fileSystem.takePersistableUriPermission(uri)
                    preferences.addRecentVaultRootUri(selectedDirectory.uri as Uri) // Use document uri instead of tree.

                    // If the user selected the vault that is already on first position, the data won't change and the
                    // flow won't emit a new value. The preselection is used to navigate there anyway.
                    state.value = LauncherState.Ready(vaultPreselectionId = vaultConfig.id)
                }
            } else if (hasNoFiles && !fileSystem.listDirectories(selectedDirectory).any { true }) {
                state.value = LauncherState.VaultCreation(selectedDirectory, uri)
            } else {
                state.value = LauncherState.DirectorySelection
            }
        }
    }

    public fun createVault(password: ByteArray) {
        GlobalScope.launch(Dispatchers.IO) {
            val directory = (state.value as LauncherState.VaultCreation).directory
            val persistableUri = (state.value as LauncherState.VaultCreation).persistableUri

            state.value = LauncherState.Processing

            val vault = vaultFactory.createVault(
                vaultDirectory = directory,
                password = password
            )

            fileSystem.takePersistableUriPermission(persistableUri)
            preferences.addRecentVaultRootUri(directory.uri as Uri) // Use document uri instead of tree.

            KoinModule.createUnlockedVaultScopeOrIgnore(vault)
            state.value = LauncherState.VaultUnlocked(vault.id)
        }
    }

    @Throws(Exception::class)
    public fun getCryptoObject(recentVaultInfo: RecentVaultInfo): BiometricPrompt.CryptoObject {
        checkNotNull(recentVaultInfo.biometricConfig)
        return biometricCryptoProvider.createDecryptingPromptObject(recentVaultInfo.biometricConfig.iv)
    }

    public fun unlockWithCipher(recentVaultInfo: RecentVaultInfo, cipher: Cipher) {
        viewModelScope.launch(Dispatchers.Default) {
            state.value = LauncherState.Processing
            unlockingError.value = null

            try {
                val password = cipher.doFinal(recentVaultInfo.biometricConfig!!.accessKey)
                unlockVaultWithPassword(recentVaultInfo, password)
            } catch (e: Exception) {
                logcat(LogPriority.ERROR) { e.asLog() }
                disableBiometricUnlockingBecauseOfError(recentVaultInfo)
                state.value = LauncherState.Ready()
            }
        }
    }

    public fun unlockVaultWithPassword(recentVaultInfo: RecentVaultInfo, password: ByteArray) {
        viewModelScope.launch(Dispatchers.Default) {
            state.value = LauncherState.Processing
            unlockingError.value = null

            try {
                val vault = vaultFactory.decryptVault(recentVaultInfo.directory, password)
                KoinModule.createUnlockedVaultScopeOrIgnore(vault)
                preferences.addRecentVaultRootUri(recentVaultInfo.directory.uri as Uri)

                state.value = LauncherState.VaultUnlocked(vault.id)
            } catch (exception: Exception) {
                logcat(LogPriority.ERROR) { exception.asLog() }
                unlockingError.value = recentVaultInfo.config.id to when (exception) {
                    is GeneralSecurityException -> R.string.incorrect_password
                    else -> R.string.error_unlocking_vault
                }

                state.value = LauncherState.Ready()
            }
        }
    }

    public fun disableBiometricUnlockingBecauseOfError(recentVaultInfo: RecentVaultInfo) {
        unlockingError.value = recentVaultInfo.config.id to R.string.biometric_unlocking_failed
    }

    public fun clearRecentVaultHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            preferences.clearRecentVaultRootUris()
        }
    }

    @Immutable
    public data class RecentVaultInfo(
        public val directory: DirectoryInfo,
        public val config: VaultConfig,
        public val biometricConfig: BiometricConfig? = null,
        @StringRes public val unlockingErrorText: Int? = null
    ) {
        public val biometricUnlockAvailable: Boolean =
            biometricConfig != null && unlockingErrorText != R.string.biometric_unlocking_failed

        init {
            require(biometricConfig == null || biometricConfig.vaultId == config.id)
        }
    }

    @Immutable
    public sealed interface LauncherState {
        public class Ready(public val vaultPreselectionId: Uuid? = null) : LauncherState
        public object Processing : LauncherState
        public object DirectorySelection : LauncherState
        public class VaultCreation(public val directory: DirectoryInfo, public val persistableUri: Uri) : LauncherState
        public class VaultUnlocked(public val vaultId: Uuid) : LauncherState
    }
}
