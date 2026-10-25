/*
 * SPDX-FileCopyrightText: 2026 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.data.io

import kotlin.time.Duration

public data class MediaProperties(
    public val duration: Duration? = null,
    public val width: Int? = null,
    public val height: Int? = null,
) {
    init {
        require(duration == null || duration >= Duration.ZERO)
        require((width == null) == (height == null))
        require((width ?: 1) > 0 && (height ?: 1) > 0)
    }
}
