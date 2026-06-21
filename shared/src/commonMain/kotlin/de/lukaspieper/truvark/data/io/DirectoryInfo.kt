/*
 * SPDX-FileCopyrightText: 2023 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.data.io

/**
 * Represents information about a directory at a certain point in time (like a snapshot).
 */
public data class DirectoryInfo(
    public val uri: Any,
    public val name: String,
) {
    init {
        require(name.isNotBlank()) { "name must not be blank" }
    }
}
