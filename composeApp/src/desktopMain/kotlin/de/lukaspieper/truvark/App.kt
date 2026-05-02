/*
 * SPDX-FileCopyrightText: 2023 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.google.crypto.tink.streamingaead.StreamingAeadConfig
import de.lukaspieper.truvark.crypto.JvmArgon2
import de.lukaspieper.truvark.data.io.JavaFileSystem
import de.lukaspieper.truvark.domain.vault.VaultFactory
import de.lukaspieper.truvark.ui.MainView
import de.lukaspieper.truvark.ui.MainViewModel
import de.lukaspieper.truvark.work.JvmScheduler
import logcat.LogcatLogger
import logcat.PrintLogger

/**
 * `gradlew desktopRun -DmainClass=de.lukaspieper.truvark.AppKt --quiet`
 */
public fun main() {
    initLogging()
    initTink()

    val fileSystem = JavaFileSystem()
    val viewModel = MainViewModel(
        fileSystem = fileSystem,
        vaultFactory = VaultFactory(
            argon2 = JvmArgon2(),
            fileSystem = fileSystem,
            scheduler = JvmScheduler()
        )
    )

    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Truvark",
            state = rememberWindowState(width = 1100.dp, height = 650.dp, position = WindowPosition(Alignment.Center))
        ) {
            MaterialTheme(
                colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme(),
                typography = typography,
                shapes = shapes
            ) {
                MainView(viewModel)
            }
        }
    }
}

private fun initLogging() {
    // TODO: PrintLogger does not seem to support minPriority
    LogcatLogger.loggers += PrintLogger

    // TODO: Enable logging based on user preferences
    LogcatLogger.install()
}

private fun initTink() {
    StreamingAeadConfig.register()
}
