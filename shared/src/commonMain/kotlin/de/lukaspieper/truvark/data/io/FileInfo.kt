/*
 * SPDX-FileCopyrightText: 2023 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.data.io

import kotlin.time.Duration

/**
 * Represents information about a file at a certain point in time (like a snapshot). For example, the size does not get
 * updated when data is written to the file.
 */
public data class FileInfo(
    public val uri: Any,
    public val fullName: String,
    public val size: Long,
    public val mimeType: String,
    public val mediaDuration: Duration? = null,
) {
    init {
        require(fullName.isNotBlank()) { "fullName must not be blank" }
        require(mimeType.isNotBlank()) { "mimeType must not be blank" }
        require(mimeType == mimeType.lowercase()) { "mimeType must be lowercase" }
    }
}
