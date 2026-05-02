/*
 * SPDX-FileCopyrightText: 2022 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.common

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
import logcat.LogPriority
import logcat.logcat
import java.security.SecureRandom

public class NotificationChannel(
    private val appContext: Context,
    private val channelId: String,
    @StringRes private val channelNameResId: Int
) {
    private val notificationManager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val random = SecureRandom()

    private val isChannelCreated by lazy { createNotificationChannel() }

    private fun createNotificationChannel(): Boolean {
        val channel = NotificationChannel(
            channelId,
            appContext.getString(channelNameResId),
            NotificationManager.IMPORTANCE_LOW
        )
        channel.lightColor = Color.GREEN
        channel.lockscreenVisibility = Notification.VISIBILITY_SECRET

        notificationManager.createNotificationChannel(channel)

        return true
    }

    public fun generateNotificationId(): Int {
        return random.nextInt()
    }

    public fun provideNotificationBuilder(): NotificationCompat.Builder {
        return NotificationCompat.Builder(appContext, channelId)
    }

    public fun notify(notificationId: Int, notification: Notification) {
        if (notificationManager.areNotificationsEnabled() && isChannelCreated) {
            notificationManager.notify(notificationId, notification)
        } else {
            logcat(LogPriority.WARN) { "Notification permission is not granted." }
        }
    }

    public fun cancel(notificationId: Int) {
        notificationManager.cancel(notificationId)
    }
}
