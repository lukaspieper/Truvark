/*
 * SPDX-FileCopyrightText: 2022 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark

import android.app.Application
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import com.google.crypto.tink.prf.PrfConfig
import com.google.crypto.tink.streamingaead.StreamingAeadConfig
import de.lukaspieper.truvark.data.preferences.PersistentPreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import logcat.AndroidLogcatLogger
import logcat.LogPriority
import logcat.LogcatLogger
import logcat.logcat
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.logger.Level

public class App : Application() {

    override fun onCreate() {
        super.onCreate()
        enableStrictModeForDebug()

        startKoin {
            if (BuildConfig.DEBUG) {
                androidLogger(Level.DEBUG)
            }

            androidContext(this@App)
            workManagerFactory()

            modules(KoinModule.module)
        }

        initLogging(get<PersistentPreferences>())

        StreamingAeadConfig.register()
        PrfConfig.register()
    }

    private fun enableStrictModeForDebug() {
        if (!BuildConfig.DEBUG) return

        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .detectAll()
                // TODO: StrictMode: Fix disk violations.
                .permitDiskReads()
                .permitDiskWrites()
                .penaltyLog()
                .build()
        )

        StrictMode.setVmPolicy(VmPolicy.Builder().detectAll().penaltyLog().build())
    }

    private fun initLogging(preferences: PersistentPreferences) {
        val isLoggingAllowed = runBlocking { preferences.loggingAllowed.first() }

        val minPriority = if (BuildConfig.DEBUG) LogPriority.VERBOSE else LogPriority.INFO
        LogcatLogger.loggers += AndroidLogcatLogger(minPriority = minPriority)

        if (isLoggingAllowed) {
            LogcatLogger.install()
        }

        // Obviously, this message is not printed when LogcatLogger is not installed.
        logcat(LogPriority.INFO) { "Logging is enabled." }
    }
}
