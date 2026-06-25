/*
 * SPDX-FileCopyrightText: 2022 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation3.ui.NavDisplay
import de.lukaspieper.truvark.ui.theme.AppTheme
import org.koin.android.ext.android.get
import org.koin.android.scope.AndroidScopeComponent
import org.koin.androidx.compose.navigation3.getEntryProvider
import org.koin.androidx.scope.activityRetainedScope
import org.koin.core.annotation.KoinDelicateAPI
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.scope.Scope

/**
 * This activity is the only one in this app.
 */
public class Activity : AppCompatActivity(), AndroidScopeComponent {

    override val scope: Scope by activityRetainedScope()

    @OptIn(KoinDelicateAPI::class, KoinExperimentalAPI::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        // Calling before onCreate because of this exception: https://stackoverflow.com/a/73129726
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                val navigator = get<Navigator>()

                // TODO: Handle multi-touch navigation conflicts
                NavDisplay(
                    backStack = navigator.backStack,
                    onBack = { navigator.goBack() },
                    entryProvider = getEntryProvider(),
                    modifier = Modifier.background(MaterialTheme.colorScheme.background)
                )
            }
        }
    }
}
