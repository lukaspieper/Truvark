/*
 * SPDX-FileCopyrightText: 2022 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

@file:UseSerializers(UuidByteArraySerializer::class)

package de.lukaspieper.truvark.domain.entities

import de.lukaspieper.truvark.data.serialization.UuidByteArraySerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.protobuf.ProtoNumber
import kotlin.time.Duration
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
@ConsistentCopyVisibility
public data class CipherFileEntity private constructor(
    @ProtoNumber(1)
    public val id: Uuid,

    @ProtoNumber(2)
    public val fullName: String,

    @ProtoNumber(3)
    public val mimeType: String,

    @ProtoNumber(4)
    public val fileSize: Long,

    @ProtoNumber(5)
    public val mediaDuration: Duration?,

    @ProtoNumber(6)
    public val creationTimestamp: Instant
) {
    @ProtoNumber(7)
    public lateinit var sha256Digest: ByteArray

    @Transient
    public lateinit var folderId: Uuid

    public constructor(
        id: Uuid,
        fullName: String,
        mimeType: String,
        fileSize: Long,
        mediaDuration: Duration?,
        creationTimestamp: Instant,
        folderId: Uuid
    ) : this(
        id = id,
        fullName = fullName,
        mimeType = mimeType,
        fileSize = fileSize,
        mediaDuration = mediaDuration,
        creationTimestamp = creationTimestamp
    ) {
        this.folderId = folderId
    }

    init {
        require(id != Uuid.NIL)
        require(fullName.isNotBlank())
        require(mimeType.isNotBlank())
        require(fileSize >= 0L)
        require(mediaDuration == null || mediaDuration >= Duration.ZERO)
    }

    public fun toOriginalFileMetadata(): OriginalFileMetadata {
        return OriginalFileMetadata(fullName, mimeType, fileSize)
    }

    // Generated
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CipherFileEntity

        if (fileSize != other.fileSize) return false
        if (id != other.id) return false
        if (fullName != other.fullName) return false
        if (mimeType != other.mimeType) return false
        if (mediaDuration != other.mediaDuration) return false
        if (creationTimestamp != other.creationTimestamp) return false
        if (!sha256Digest.contentEquals(other.sha256Digest)) return false
        if (folderId != other.folderId) return false

        return true
    }

    // Generated
    override fun hashCode(): Int {
        var result = fileSize.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + fullName.hashCode()
        result = 31 * result + mimeType.hashCode()
        result = 31 * result + (mediaDuration?.hashCode() ?: 0)
        result = 31 * result + creationTimestamp.hashCode()
        result = 31 * result + sha256Digest.contentHashCode()
        result = 31 * result + folderId.hashCode()
        return result
    }

    // Generated
    override fun toString(): String {
        return "CipherFileEntity(id=$id, fullName='$fullName', mimeType='$mimeType', fileSize=$fileSize, mediaDuration=$mediaDuration, creationTimestamp=$creationTimestamp, sha256Digest=${sha256Digest.contentToString()}, folderId=$folderId)"
    }
}
