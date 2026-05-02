/*
 * SPDX-FileCopyrightText: 2026 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.domain.crypto.decryption.coil

import coil3.key.Keyer
import coil3.request.Options
import de.lukaspieper.truvark.data.io.FileInfo

public class FileInfoKeyer : Keyer<FileInfo> {

    override fun key(data: FileInfo, options: Options): String {
        return data.fullName
    }
}
