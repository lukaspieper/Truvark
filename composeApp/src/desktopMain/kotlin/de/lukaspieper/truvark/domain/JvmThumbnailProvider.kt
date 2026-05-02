/*
 * SPDX-FileCopyrightText: 2023 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.domain

import de.lukaspieper.truvark.data.io.FileInfo

public class JvmThumbnailProvider : ThumbnailProvider {

    override suspend fun createThumbnail(file: FileInfo): ByteArray? {
        // TODO: Implement thumbnail provider for Java
        return null
    }
}
