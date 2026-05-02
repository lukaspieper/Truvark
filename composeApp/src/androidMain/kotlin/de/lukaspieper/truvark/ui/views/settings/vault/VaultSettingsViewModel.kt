/*
 * SPDX-FileCopyrightText: 2022 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.ui.views.settings.vault

import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import de.lukaspieper.truvark.data.preferences.PersistentPreferences
import de.lukaspieper.truvark.domain.crypto.BiometricConfig
import de.lukaspieper.truvark.domain.crypto.BiometricCryptoProvider
import de.lukaspieper.truvark.domain.vault.Vault
import de.lukaspieper.truvark.domain.vault.VaultFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import logcat.LogPriority
import logcat.asLog
import logcat.logcat
import javax.crypto.Cipher
import javax.inject.Inject

@HiltViewModel
public class VaultSettingsViewModel @Inject constructor(
    private val vaultFactory: VaultFactory,
    private val vault: Vault,
    private val biometricCryptoProvider: BiometricCryptoProvider,
    private val preferences: PersistentPreferences
) : ViewModel() {
    public val isVaultUsingBiometricUnlocking: Flow<Boolean> =
        preferences.biometricConfig.map { it?.vaultId == vault.id }

    public var vaultName: String by mutableStateOf(vault.displayName)
        private set

    public fun updateVaultName(name: String): Boolean {
        try {
            vault.updateDisplayName(name)
            vaultName = vault.displayName
            return true
        } catch (_: Exception) {
            // Not logging here because serious errors are already logged at this time.
            return false
        }
    }

    public fun checkBiometricSupport(): Int {
        return biometricCryptoProvider.checkBiometricSupport()
    }

    public suspend fun setupBiometricUnlocking(
        password: ByteArray,
        authenticateCryptoObject: suspend (BiometricPrompt.CryptoObject) -> Cipher?
    ): BiometricSetupResult {
        try {
            if (!vaultFactory.validatePassword(vault, password)) {
                return BiometricSetupResult.INVALID_PASSWORD
            }

            val cryptoObject = biometricCryptoProvider.createEncryptingPromptObject()
            val cipher = authenticateCryptoObject(cryptoObject)
            checkNotNull(cipher) { "Authentication failed" }

            val config = BiometricConfig(
                vaultId = vault.id,
                iv = cipher.iv,
                accessKey = cipher.doFinal(password)
            )
            preferences.saveBiometricConfig(config)

            return BiometricSetupResult.SUCCESS
        } catch (e: Exception) {
            logcat(LogPriority.WARN) { e.asLog() }
            return BiometricSetupResult.UNKNOWN_ERROR
        }
    }

    public enum class BiometricSetupResult {
        SUCCESS,
        INVALID_PASSWORD,
        UNKNOWN_ERROR
    }
}
