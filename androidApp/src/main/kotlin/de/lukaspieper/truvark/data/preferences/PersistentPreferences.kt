/*
 * SPDX-FileCopyrightText: 2022 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.data.preferences

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.byteArrayPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import de.lukaspieper.truvark.domain.crypto.BiometricConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "AppPreferences")

/**
 * Persistent preferences based on [DataStore].
 */
public class PersistentPreferences(context: Context) {
    private val dataStore = context.dataStore

    private companion object {
        val LAST_USED_VAULT_ROOT_URI = stringPreferencesKey("PREF_LAST_USED_VAULT_ROOT_URI")
        val BIOMETRIC_CONFIG = byteArrayPreferencesKey("PREF_BIOMETRIC_CONFIG")
        val LOGGING_ALLOWED = booleanPreferencesKey("PREF_LOGGING_ALLOWED")
        val IS_LIST_LAYOUT = booleanPreferencesKey("PREF_IS_LIST_LAYOUT")
        val IMAGES_FIT_SCREEN = booleanPreferencesKey("PREF_IMAGES_FIT_SCREEN")
    }

    public suspend fun saveLastUsedVaultRootUri(uri: Uri) {
        dataStore.edit { preferences ->
            preferences[LAST_USED_VAULT_ROOT_URI] = uri.toString()
        }
    }

    public val lastUsedVaultRootUri: Flow<Uri> = dataStore.data.map { preferences ->
        val lastUsedVaultRootUri = preferences[LAST_USED_VAULT_ROOT_URI]

        when {
            lastUsedVaultRootUri.isNullOrBlank() -> Uri.EMPTY
            else -> lastUsedVaultRootUri.toUri()
        }
    }

    public suspend fun saveBiometricConfig(config: BiometricConfig) {
        dataStore.edit { preferences ->
            preferences[BIOMETRIC_CONFIG] = config.toByteArray()
        }
    }

    public val biometricConfig: Flow<BiometricConfig?> = dataStore.data.map { preferences ->
        val bytes = preferences[BIOMETRIC_CONFIG]

        when {
            bytes == null || bytes.isEmpty() -> null
            else -> BiometricConfig.fromByteArray(bytes)
        }
    }

    public suspend fun saveLoggingAllowed(allowed: Boolean) {
        dataStore.edit { preferences ->
            preferences[LOGGING_ALLOWED] = allowed
        }
    }

    public val loggingAllowed: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[LOGGING_ALLOWED] ?: false
    }

    public suspend fun saveIsListLayout(isListLayout: Boolean) {
        dataStore.edit { preferences ->
            preferences[IS_LIST_LAYOUT] = isListLayout
        }
    }

    public val isListLayout: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[IS_LIST_LAYOUT] ?: false
    }

    public suspend fun saveImagesFitScreen(fitScreen: Boolean) {
        dataStore.edit { preferences ->
            preferences[IMAGES_FIT_SCREEN] = fitScreen
        }
    }

    public val imagesFitScreen: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[IMAGES_FIT_SCREEN] ?: true
    }
}
