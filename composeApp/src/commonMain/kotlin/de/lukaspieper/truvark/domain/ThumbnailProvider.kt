/*
 * SPDX-FileCopyrightText: 2022 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.domain

import de.lukaspieper.truvark.data.io.FileInfo

public interface ThumbnailProvider {
    public suspend fun createThumbnail(file: FileInfo): ByteArray?
}
