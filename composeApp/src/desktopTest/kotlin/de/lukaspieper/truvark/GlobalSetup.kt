/*
 * SPDX-FileCopyrightText: 2026 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark

import com.google.crypto.tink.prf.PrfConfig
import com.google.crypto.tink.streamingaead.StreamingAeadConfig
import logcat.LogcatLogger
import logcat.PrintLogger
import org.junit.platform.launcher.LauncherSession
import org.junit.platform.launcher.LauncherSessionListener

class GlobalSetup : LauncherSessionListener {

    override fun launcherSessionOpened(session: LauncherSession) {
        LogcatLogger.loggers += PrintLogger
        LogcatLogger.install()

        StreamingAeadConfig.register()
        PrfConfig.register()
    }
}
