/*
 * SPDX-FileCopyrightText: 2026 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.data.preferences.migrations

import androidx.datastore.core.DataMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import de.lukaspieper.truvark.data.preferences.PreferencesKeys.RECENT_VAULT_ROOT_URIS
import de.lukaspieper.truvark.data.preferences.models.RecentVaultRootUris

internal object RecentlyUsedVaultRootUrisMigration : DataMigration<Preferences> {
    private val LAST_USED_VAULT_ROOT_URI = stringPreferencesKey("PREF_LAST_USED_VAULT_ROOT_URI")

    override suspend fun shouldMigrate(currentData: Preferences): Boolean {
        return currentData[LAST_USED_VAULT_ROOT_URI] != null
    }

    override suspend fun migrate(currentData: Preferences): Preferences {
        val legacyUri = currentData[LAST_USED_VAULT_ROOT_URI]!!

        val migratedPreferences = currentData.toMutablePreferences().apply {
            remove(LAST_USED_VAULT_ROOT_URI)
        }

        if (legacyUri.isNotBlank()) {
            migratedPreferences[RECENT_VAULT_ROOT_URIS] = RecentVaultRootUris(listOf(legacyUri)).toByteArray()
        }

        return migratedPreferences
    }

    override suspend fun cleanUp() {
        // No access to the Preferences object here, so we cannot remove the legacy key here.
    }
}
