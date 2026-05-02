/*
 * SPDX-FileCopyrightText: 2022 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.work

import android.content.Context
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
import android.os.Build
import androidx.annotation.StringRes
import androidx.hilt.work.HiltWorker
import androidx.work.Data
import androidx.work.ForegroundInfo
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.runBlocking
import logcat.LogPriority.DEBUG
import logcat.LogPriority.ERROR
import logcat.LogPriority.INFO
import logcat.asLog
import logcat.logcat
import kotlin.system.measureTimeMillis

@HiltWorker
public open class UniversalWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParameters: WorkerParameters,
    protected val workScheduler: WorkScheduler
) : Worker(appContext, workerParameters) {

    protected val notificationId: Int = inputData.keyValueMap[DATA_KEY_NOTIFICATION_ID] as Int

    @StringRes
    protected val contentTitleResId: Int = inputData.keyValueMap[DATA_KEY_CONTENT_TITLE_RES_ID] as Int

    final override fun doWork(): Result {
        val elapsedMilliseconds = measureTimeMillis {
            try {
                runBlocking { tryDoWork() }
            } catch (e: Exception) {
                logcat(ERROR) { e.asLog() }
            } finally {
                // TODO: Stick with onStopped() or define own method?
                onStopped()
            }
        }
        logcat(INFO) { "Worker took $elapsedMilliseconds milliseconds." }

        // Always returning success because dependent work requests need to run in any case
        return Result.success()
    }

    public open suspend fun tryDoWork() {
        workScheduler.processWork(notificationId, contentTitleResId)
    }

    override fun getForegroundInfo(): ForegroundInfo {
        logcat(DEBUG) { "Worker requesting ForegroundInfo." }

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ForegroundInfo(
                notificationId,
                workScheduler.buildUpdatedNotification(notificationId, contentTitleResId)!!,
                FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            ForegroundInfo(
                notificationId,
                workScheduler.buildUpdatedNotification(notificationId, contentTitleResId)!!
            )
        }
    }

    public companion object {
        private const val DATA_KEY_NOTIFICATION_ID = "NOTIFICATION_ID"
        private const val DATA_KEY_CONTENT_TITLE_RES_ID = "CONTENT_TITLE_RES_ID"

        public fun createInputData(notificationId: Int, @StringRes contentTitleResId: Int): Data {
            return workDataOf(
                DATA_KEY_NOTIFICATION_ID to notificationId,
                DATA_KEY_CONTENT_TITLE_RES_ID to contentTitleResId
            )
        }
    }
}
