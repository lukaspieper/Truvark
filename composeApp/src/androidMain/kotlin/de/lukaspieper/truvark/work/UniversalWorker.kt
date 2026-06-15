/*
 * SPDX-FileCopyrightText: 2022 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.work

import android.content.Context
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
import android.os.Build
import androidx.work.Data
import androidx.work.ForegroundInfo
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.runBlocking
import logcat.LogPriority.DEBUG
import logcat.LogPriority.ERROR
import logcat.LogPriority.INFO
import logcat.asLog
import logcat.logcat
import kotlin.system.measureTimeMillis

public class UniversalWorker(
    appContext: Context,
    workerParameters: WorkerParameters,
    private val workScheduler: WorkScheduler
) : Worker(appContext, workerParameters) {

    private val notificationId: Int = inputData.keyValueMap[DATA_KEY_NOTIFICATION_ID] as Int

    override fun doWork(): Result {
        val elapsedMilliseconds = measureTimeMillis {
            try {
                runBlocking {
                    workScheduler.processWork(notificationId)
                }
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

    override fun getForegroundInfo(): ForegroundInfo {
        logcat(DEBUG) { "Worker requesting ForegroundInfo." }

        val notification = workScheduler.buildUpdatedNotification(notificationId)!!
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ForegroundInfo(notificationId, notification, FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            ForegroundInfo(notificationId, notification)
        }
    }

    public companion object {
        private const val DATA_KEY_NOTIFICATION_ID = "NOTIFICATION_ID"

        public fun createInputData(notificationId: Int): Data {
            return workDataOf(DATA_KEY_NOTIFICATION_ID to notificationId)
        }
    }
}
