/*
 * SPDX-FileCopyrightText: 2026 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.data.preferences.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoBuf
import kotlinx.serialization.protobuf.ProtoNumber

@Serializable
public data class RecentVaultRootUris(
    @ProtoNumber(1)
    val uris: List<String>
) {
    public companion object {
        @Throws(Exception::class)
        internal fun fromByteArray(bytes: ByteArray): RecentVaultRootUris {
            return ProtoBuf.decodeFromByteArray(serializer(), bytes)
        }
    }

    @Throws(Exception::class)
    internal fun toByteArray(): ByteArray {
        return ProtoBuf.encodeToByteArray(serializer(), this)
    }
}
