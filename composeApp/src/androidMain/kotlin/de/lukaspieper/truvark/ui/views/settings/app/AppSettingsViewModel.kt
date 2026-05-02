/*
 * SPDX-FileCopyrightText: 2022 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.ui.views.settings.app

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import de.lukaspieper.truvark.data.preferences.PersistentPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking
import logcat.LogcatLogger
import javax.inject.Inject

@HiltViewModel
public class AppSettingsViewModel @Inject constructor(
    private val preferences: PersistentPreferences
) : ViewModel() {
    public val isLoggingEnabled: Flow<Boolean> = preferences.loggingAllowed
    public val imagesFitScreen: Flow<Boolean> = preferences.imagesFitScreen

    public fun applyLogging(enabled: Boolean) {
        runBlocking {
            when {
                enabled -> LogcatLogger.install()
                else -> LogcatLogger.uninstall()
            }

            preferences.saveLoggingAllowed(enabled)
        }
    }

    public fun applyImagesFitScreen(enabled: Boolean) {
        runBlocking {
            preferences.saveImagesFitScreen(enabled)
        }
    }
}
