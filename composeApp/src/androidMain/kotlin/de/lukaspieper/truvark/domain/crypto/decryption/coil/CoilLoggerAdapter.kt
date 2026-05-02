/*
 * SPDX-FileCopyrightText: 2026 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.domain.crypto.decryption.coil

import coil3.util.Logger
import logcat.LogPriority
import logcat.asLog
import logcat.logcat

public class CoilLoggerAdapter(override var minLevel: Logger.Level = Logger.Level.Verbose) : Logger {

    override fun log(
        tag: String,
        level: Logger.Level,
        message: String?,
        throwable: Throwable?
    ) {
        val priority = convertLogLevelToLogPriority(level)

        message?.let {
            logcat(priority, tag) { it }
        }

        throwable?.let {
            logcat(priority, tag) { it.asLog() }
        }
    }

    private fun convertLogLevelToLogPriority(level: Logger.Level): LogPriority {
        return when (level) {
            Logger.Level.Verbose -> LogPriority.VERBOSE
            Logger.Level.Debug -> LogPriority.DEBUG
            Logger.Level.Info -> LogPriority.INFO
            Logger.Level.Warn -> LogPriority.WARN
            Logger.Level.Error -> LogPriority.ERROR
        }
    }
}
