/*
 * SPDX-FileCopyrightText: 2022 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

@file:UseSerializers(UuidByteArraySerializer::class)

package de.lukaspieper.truvark.domain.entities

import de.lukaspieper.truvark.data.serialization.UuidByteArraySerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.protobuf.ProtoNumber
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
public data class CipherFolderEntity(
    @ProtoNumber(1)
    public val id: Uuid,

    @ProtoNumber(2)
    public val displayName: String,

    @ProtoNumber(3)
    public val parentFolderId: Uuid,

    @ProtoNumber(4)
    public val creationTimestamp: Instant
) {
    init {
        require(displayName.isNotBlank())
    }
}
