/*
 * SPDX-FileCopyrightText: 2022 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.domain.entities

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoBuf
import kotlinx.serialization.protobuf.ProtoNumber

/***
 * Bundle of information about the original file.
 */
@Serializable
public data class OriginalFileMetadata(
    @ProtoNumber(1)
    public val fullName: String,

    @ProtoNumber(2)
    public val mimeType: String,

    @ProtoNumber(3)
    public val fileSize: Long
) {
    public companion object {
        @Throws(Exception::class)
        public fun fromByteArray(bytes: ByteArray): OriginalFileMetadata {
            return ProtoBuf.decodeFromByteArray(serializer(), bytes)
        }
    }

    init {
        require(fullName.isNotBlank())
        require(mimeType.isNotBlank())
        require(fileSize >= 0L)
    }

    @Throws(Exception::class)
    internal fun toByteArray(): ByteArray {
        return ProtoBuf.encodeToByteArray(serializer(), this)
    }
}
