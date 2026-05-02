/*
 * SPDX-FileCopyrightText: 2023 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.test.doubles

import de.lukaspieper.truvark.data.io.FileInfo
import de.lukaspieper.truvark.domain.ThumbnailProvider

class ThumbnailProviderFake : ThumbnailProvider {

    override suspend fun createThumbnail(file: FileInfo): ByteArray? {
        return null
    }
}
