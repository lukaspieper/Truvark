/*
 * SPDX-FileCopyrightText: 2022 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.work

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkQuery
import androidx.work.Worker
import de.lukaspieper.truvark.R
import de.lukaspieper.truvark.common.NotificationChannel
import de.lukaspieper.truvark.di.VaultModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import logcat.LogPriority
import logcat.logcat

/**
 * A [WorkManager] that schedules [WorkBundle]s. For running work, notifications are shown.
 *
 * Note that most workers require [VaultModule.initializeVaultModule] to be executed before.
 */
public class WorkScheduler(private val appContext: Context) : Scheduler {

    private companion object {
        const val CHANNEL_ID = "de.lukaspieper.truvark"

        /**
         * Just any constant name that is used for enqueuing all [Worker]s to prevent running multiple operations in
         * the same folder or even on the same files.
         */
        const val UNIQUE_WORK_NAME = "UNIQUE_WORK_NAME"
    }

    private val workManager = WorkManager.getInstance(appContext)
    private val notificationChannel = NotificationChannel(appContext, CHANNEL_ID, R.string.foreground_service)

    private val scheduledBundles = mutableMapOf<Int, ScheduledBundle>()

    init {
        val enqueuedWorkQuery = WorkQuery.fromStates(WorkInfo.State.ENQUEUED)
        val enqueuedWorkInfo = workManager.getWorkInfos(enqueuedWorkQuery).get()
        logcat(LogPriority.WARN) {
            "Number of enqueued work that did not run before being canceled: ${enqueuedWorkInfo.size}"
        }

        // The user might have switched the vault. To avoid any side effects all work will be canceled.
        workManager.cancelAllWork()

        // Delete data from eligible finished work because failed work cannot be retried (SAF permissions, etc).
        workManager.pruneWork()
    }

    override fun schedule(workBundle: WorkBundle) {
        require(workBundle.properties is NotificationProperties)

        val scheduledBundle = ScheduledBundle(appContext, workBundle, notificationChannel.provideNotificationBuilder())

        var notificationId: Int
        do {
            notificationId = notificationChannel.generateNotificationId()
        } while (scheduledBundles.containsKey(notificationId))

        scheduledBundles[notificationId] = scheduledBundle
        notificationChannel.notify(notificationId, scheduledBundle.buildNotification()!!)

        val workRequests = MutableList(workBundle.size) {
            OneTimeWorkRequestBuilder<UniversalWorker>()
                .setInputData(UniversalWorker.createInputData(notificationId))
                .addTag(notificationId.toString())
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .build()
        }
        workManager.enqueueUniqueWork(UNIQUE_WORK_NAME, ExistingWorkPolicy.APPEND_OR_REPLACE, workRequests)

        scheduledBundle.updateNotificationJob = CoroutineScope(Dispatchers.Default).launch {
            workBundle.progress.collect { progress ->
                val notification = buildUpdatedNotification(notificationId)

                // Some operations might keep the notification after finishing. Therefore, the cleanup is separated.
                if (notification != null) {
                    notificationChannel.notify(notificationId, notification)
                } else {
                    notificationChannel.cancel(notificationId)
                }

                // Cleaning up no matter if the notification is kept or not.
                if (progress == workBundle.size) {
                    scheduledBundle.updateNotificationJob.cancel()
                    scheduledBundles.remove(notificationId)
                }
            }
        }
    }

    public suspend fun processWork(notificationId: Int) {
        scheduledBundles[notificationId]?.workBundle?.processUnit()
    }

    public fun buildUpdatedNotification(notificationId: Int): Notification? {
        val scheduledBundle = scheduledBundles[notificationId] ?: return null
        return scheduledBundle.buildNotification()
    }

    public data class NotificationProperties(
        @StringRes val notificationTitle: Int,
        @StringRes val notificationFinishTitle: Int? = null,
        val notificationAction: Intent? = null,
        @StringRes val notificationActionText: Int? = null,
    ) : WorkBundle.Properties

    private data class ScheduledBundle(
        private val context: Context,
        val workBundle: WorkBundle,
        private val notificationBuilder: NotificationCompat.Builder,
    ) {
        private val properties = workBundle.properties as NotificationProperties

        lateinit var updateNotificationJob: Job

        init {
            with(notificationBuilder) {
                setSmallIcon(R.drawable.ic_truvark)
                setCategory(Notification.CATEGORY_SERVICE)
                setOnlyAlertOnce(true)
                setOngoing(true)
                setVisibility(NotificationCompat.VISIBILITY_SECRET)
                setProgress(0, 0, true)
            }
        }

        fun buildNotification(): Notification? {
            if (workBundle.progress.value == workBundle.size) {
                return buildFinishNotification()
            }

            var contentTitle = context.getString(properties.notificationTitle)

            with(workBundle) {
                if (size > 0 && progress.value > 0) {
                    contentTitle = "$contentTitle (${progress.value}/$size)"
                    notificationBuilder.setProgress(size, progress.value, false)
                }
            }

            notificationBuilder.setContentTitle(contentTitle)
            return notificationBuilder.build()
        }

        private fun buildFinishNotification(): Notification? {
            if (properties.notificationFinishTitle == null) {
                return null
            }

            return notificationBuilder.apply {
                setContentTitle(context.getString(properties.notificationFinishTitle))
                setProgress(0, 0, false)
                setOngoing(false)
                setAutoCancel(true)

                properties.notificationAction?.let { intent ->
                    val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
                    setContentIntent(pendingIntent)

                    properties.notificationActionText?.let { actionText ->
                        addAction(R.drawable.ic_truvark, context.getString(actionText), pendingIntent)
                    }
                }
            }.build()
        }
    }
}
