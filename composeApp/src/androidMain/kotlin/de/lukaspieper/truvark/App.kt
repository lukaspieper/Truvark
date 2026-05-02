/*
 * SPDX-FileCopyrightText: 2022 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark

import android.app.Application
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.google.crypto.tink.streamingaead.StreamingAeadConfig
import dagger.hilt.android.HiltAndroidApp
import de.lukaspieper.truvark.data.preferences.PersistentPreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import logcat.AndroidLogcatLogger
import logcat.LogPriority
import logcat.LogcatLogger
import logcat.logcat
import javax.inject.Inject

@HiltAndroidApp
public class App : Application(), Configuration.Provider {

    @Inject
    public lateinit var preferences: PersistentPreferences

    @Inject
    public lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(workerFactory).build()

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            // StrictMode.enableDefaults();

            StrictMode.setVmPolicy(
                VmPolicy.Builder()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .build()
            )
        }

        initLogging()
        initTink()
    }

    private fun initLogging() {
        val isLoggingAllowed = runBlocking { preferences.loggingAllowed.first() }

        val minPriority = if (BuildConfig.DEBUG) LogPriority.VERBOSE else LogPriority.INFO
        LogcatLogger.loggers += AndroidLogcatLogger(minPriority = minPriority)

        if (isLoggingAllowed) {
            LogcatLogger.install()
        }

        // Obviously, this message is not printed when LogcatLogger is not installed.
        logcat(LogPriority.INFO) { "Logging is enabled." }
    }

    private fun initTink() {
        StreamingAeadConfig.register()
    }
}
