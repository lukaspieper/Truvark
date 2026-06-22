/*
 * SPDX-FileCopyrightText: 2024 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.test.doubles

import de.lukaspieper.truvark.work.Scheduler
import de.lukaspieper.truvark.work.WorkBundle
import kotlinx.coroutines.runBlocking

/**
 * A fake implementation of [Scheduler] that processes all [WorkBundle] units synchronously (blocking).
 */
class SchedulerFake : Scheduler {

    override fun schedule(workBundle: WorkBundle) {
        runBlocking {
            for (i in 0 until workBundle.size) {
                workBundle.processUnit()
            }
        }
    }

    object EmptyProperties : WorkBundle.Properties
}
