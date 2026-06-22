/*
 * SPDX-FileCopyrightText: 2025 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.data.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ByteArraySerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.uuid.Uuid

public object UuidByteArraySerializer : KSerializer<Uuid> {
    private val serializer = ByteArraySerializer()
    override val descriptor: SerialDescriptor = SerialDescriptor("kotlin.uuid.Uuid.binary", serializer.descriptor)

    override fun serialize(encoder: Encoder, value: Uuid) {
        val byteArray = value.toByteArray()
        encoder.encodeSerializableValue(serializer, byteArray)
    }

    override fun deserialize(decoder: Decoder): Uuid {
        val byteArray = decoder.decodeSerializableValue(serializer)
        return Uuid.fromByteArray(byteArray)
    }
}
