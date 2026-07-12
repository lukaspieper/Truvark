/*
 * SPDX-FileCopyrightText: 2026 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.data.preferences

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.byteArrayPreferencesKey

internal object PreferencesKeys {
    val RECENT_VAULT_ROOT_URIS = byteArrayPreferencesKey("PREF_RECENT_VAULT_ROOT_URIS")
    val BIOMETRIC_CONFIG = byteArrayPreferencesKey("PREF_BIOMETRIC_CONFIG")
    val LOGGING_ALLOWED = booleanPreferencesKey("PREF_LOGGING_ALLOWED")
    val IS_LIST_LAYOUT = booleanPreferencesKey("PREF_IS_LIST_LAYOUT")
    val IMAGES_FIT_SCREEN = booleanPreferencesKey("PREF_IMAGES_FIT_SCREEN")
    val ALLOW_SCREEN_CAPTURE = booleanPreferencesKey("PREF_ALLOW_SCREEN_CAPTURE")
}
