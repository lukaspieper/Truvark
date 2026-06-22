/*
 * SPDX-FileCopyrightText: 2022 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

@file:UseSerializers(UuidByteArraySerializer::class)

package de.lukaspieper.truvark.domain.vault

import de.lukaspieper.truvark.data.serialization.UuidByteArraySerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.protobuf.ProtoBuf
import kotlinx.serialization.protobuf.ProtoNumber
import kotlin.uuid.Uuid

@Serializable
@ConsistentCopyVisibility
public data class VaultConfig internal constructor(
    @ProtoNumber(1)
    public val id: Uuid,

    @ProtoNumber(2)
    public val name: String,

    @ProtoNumber(3)
    internal val argon2Config: String,

    @ProtoNumber(4)
    internal val encryptedStreamingAeadKeyset: ByteArray,

    @ProtoNumber(5)
    internal val encryptedPrfKeyset: ByteArray
) {
    public companion object {
        public const val FILENAME: String = "vault"

        // The value is arbitrary chosen and still too high to display the name in the UI.
        public const val MAX_VAULT_NAME_LENGTH: Int = 64

        // TODO: Find a better place for the 'MIN_PASSWORD_LENGTH' constant?
        public const val MIN_PASSWORD_LENGTH: Int = 8

        @Throws(Exception::class)
        internal fun fromByteArray(bytes: ByteArray): VaultConfig {
            return ProtoBuf.decodeFromByteArray(serializer(), bytes)
        }
    }

    init {
        require(id != Uuid.NIL)
        require(name.isNotBlank())
        require(name.length in 1..MAX_VAULT_NAME_LENGTH)
        require(argon2Config.isNotBlank())
        require(encryptedStreamingAeadKeyset.isNotEmpty())
        require(encryptedPrfKeyset.isNotEmpty())
    }

    @Throws(Exception::class)
    internal fun toByteArray(): ByteArray {
        return ProtoBuf.encodeToByteArray(serializer(), this)
    }

    // Generated
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as VaultConfig

        if (id != other.id) return false
        if (name != other.name) return false
        if (argon2Config != other.argon2Config) return false
        if (!encryptedStreamingAeadKeyset.contentEquals(other.encryptedStreamingAeadKeyset)) return false
        if (!encryptedPrfKeyset.contentEquals(other.encryptedPrfKeyset)) return false

        return true
    }

    // Generated
    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + argon2Config.hashCode()
        result = 31 * result + encryptedStreamingAeadKeyset.contentHashCode()
        result = 31 * result + encryptedPrfKeyset.contentHashCode()
        return result
    }
}
