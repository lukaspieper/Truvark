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
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import de.lukaspieper.truvark.data.preferences.PreferencesKeys.ALLOW_SCREEN_CAPTURE
import de.lukaspieper.truvark.data.preferences.PreferencesKeys.BIOMETRIC_CONFIG
import de.lukaspieper.truvark.data.preferences.PreferencesKeys.IMAGES_FIT_SCREEN
import de.lukaspieper.truvark.data.preferences.PreferencesKeys.IS_LIST_LAYOUT
import de.lukaspieper.truvark.data.preferences.PreferencesKeys.LOGGING_ALLOWED
import de.lukaspieper.truvark.data.preferences.PreferencesKeys.RECENT_VAULT_ROOT_URIS
import de.lukaspieper.truvark.data.preferences.migrations.RecentlyUsedVaultRootUrisMigration
import de.lukaspieper.truvark.data.preferences.models.BiometricConfig
import de.lukaspieper.truvark.data.preferences.models.RecentVaultRootUris
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(
    name = "AppPreferences",
    produceMigrations = { _ -> listOf(RecentlyUsedVaultRootUrisMigration) }
)

/**
 * Persistent preferences based on [DataStore].
 */
public class PersistentPreferences(context: Context) {
    private val dataStore = context.dataStore

    public suspend fun addRecentVaultRootUri(uri: Uri) {
        if (uri == Uri.EMPTY) return

        dataStore.edit { preferences ->
            val updatedUris = (listOf(uri.toString()) + preferences.readRecentVaultRootUris())
                .distinct()
                .take(3)

            preferences[RECENT_VAULT_ROOT_URIS] = RecentVaultRootUris(updatedUris).toByteArray()
        }
    }

    public suspend fun clearRecentVaultRootUris() {
        dataStore.edit { preferences ->
            preferences[RECENT_VAULT_ROOT_URIS] = RecentVaultRootUris(emptyList()).toByteArray()
        }
    }

    public val recentVaultRootUris: Flow<List<Uri>> = dataStore.data.map { preferences ->
        preferences.readRecentVaultRootUris().map { it.toUri() }
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

    public suspend fun saveAllowScreenCapture(allowed: Boolean) {
        dataStore.edit { preferences ->
            preferences[ALLOW_SCREEN_CAPTURE] = allowed
        }
    }

    public val allowScreenCapture: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[ALLOW_SCREEN_CAPTURE] ?: false
    }

    // Computed "preferences" based on other preferences

    public val isAnyDebuggingSettingEnabled: Flow<Boolean> = combine(
        loggingAllowed,
        allowScreenCapture
    ) { loggingAllowed, allowScreenCapture ->
        loggingAllowed || allowScreenCapture
    }

    // Extension functions

    private fun Preferences.readRecentVaultRootUris(): List<String> {
        val bytes = this[RECENT_VAULT_ROOT_URIS]

        return when {
            bytes == null || bytes.isEmpty() -> emptyList()
            else -> RecentVaultRootUris.fromByteArray(bytes).uris
        }
    }
}
