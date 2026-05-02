/*
 * SPDX-FileCopyrightText: 2022 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

@file:UseSerializers(UuidByteArraySerializer::class)

package de.lukaspieper.truvark.domain.crypto

import de.lukaspieper.truvark.data.serialization.UuidByteArraySerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.protobuf.ProtoBuf
import kotlinx.serialization.protobuf.ProtoNumber
import kotlin.uuid.Uuid

@Serializable
public data class BiometricConfig(
    @ProtoNumber(1)
    val vaultId: Uuid,

    @ProtoNumber(2)
    val iv: ByteArray,

    @ProtoNumber(3)
    val accessKey: ByteArray
) {
    public companion object {

        @Throws(Exception::class)
        internal fun fromByteArray(bytes: ByteArray): BiometricConfig {
            return ProtoBuf.decodeFromByteArray(serializer(), bytes)
        }
    }

    init {
        require(vaultId != Uuid.NIL)
        require(iv.isNotEmpty())
        require(accessKey.isNotEmpty())
    }

    @Throws(Exception::class)
    internal fun toByteArray(): ByteArray {
        return ProtoBuf.encodeToByteArray(serializer(), this)
    }

    // generated
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BiometricConfig

        if (vaultId != other.vaultId) return false
        if (!iv.contentEquals(other.iv)) return false
        if (!accessKey.contentEquals(other.accessKey)) return false

        return true
    }

    // generated
    override fun hashCode(): Int {
        var result = vaultId.hashCode()
        result = 31 * result + iv.contentHashCode()
        result = 31 * result + accessKey.contentHashCode()
        return result
    }
}
