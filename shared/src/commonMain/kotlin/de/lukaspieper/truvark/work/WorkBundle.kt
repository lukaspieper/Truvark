/*
 * SPDX-FileCopyrightText: 2024 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.work

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate

public abstract class WorkBundle {
    private val _progress = MutableStateFlow(0)

    public abstract val properties: Properties
    public abstract val size: Int
    public val progress: StateFlow<Int> = _progress.asStateFlow()

    public suspend fun processUnit() {
        processUnitAtIndex(_progress.getAndUpdate { it + 1 })
    }

    protected abstract suspend fun processUnitAtIndex(index: Int)

    /**
     * Empty interface to supply additional data through the work bundle to the [Scheduler].
     */
    public interface Properties
}
