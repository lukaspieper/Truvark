/*
 * SPDX-FileCopyrightText: 2026 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.data.io

import android.content.Context
import android.media.MediaMetadataRetriever
import android.media.MediaMetadataRetriever.METADATA_KEY_DURATION
import android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT
import android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION
import android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import logcat.LogPriority
import logcat.asLog
import logcat.logcat
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * Reads media properties from Android content URIs. [MediaProperties.width] and [MediaProperties.height] are the
 * dimensions as displayed, i.e. with rotation applied.
 */
internal class MediaPropertiesReader(private val context: Context) {
    private companion object {
        const val HALF_TURN_DEGREES = 180
    }

    fun read(fileInfo: FileInfo): MediaProperties {
        val uri = fileInfo.uri as Uri

        return try {
            when {
                fileInfo.mimeType.startsWith("video/") -> readMediaMetadata(uri, isAudio = false)
                fileInfo.mimeType.startsWith("audio/") -> readMediaMetadata(uri, isAudio = true)
                fileInfo.mimeType.startsWith("image/") -> readExifMetadata(uri)
                else -> MediaProperties()
            }
        } catch (e: Exception) {
            logcat(LogPriority.WARN) { e.asLog() }
            MediaProperties()
        }
    }

    private fun readMediaMetadata(uri: Uri, isAudio: Boolean): MediaProperties {
        return MediaMetadataRetriever().use { retriever ->
            retriever.setDataSource(context, uri)

            val duration = retriever.extractMetadata(METADATA_KEY_DURATION)
                ?.toLongOrNull()
                ?.takeIf { it >= 0L }
                ?.milliseconds
            if (isAudio) return@use MediaProperties(duration)

            createWithDimensions(
                width = retriever.extractMetadata(METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull() ?: 0,
                height = retriever.extractMetadata(METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull() ?: 0,
                rotationDegrees = retriever.extractMetadata(METADATA_KEY_VIDEO_ROTATION)?.toIntOrNull(),
                duration = duration
            )
        }
    }

    private fun readExifMetadata(uri: Uri): MediaProperties {
        val exif = context.contentResolver.openInputStream(uri)?.use { ExifInterface(it) }
            ?: return MediaProperties()

        return createWithDimensions(
            width = exif.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, 0),
            height = exif.getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH, 0),
            rotationDegrees = exif.rotationDegrees
        )
    }

    private fun createWithDimensions(
        width: Int,
        height: Int,
        rotationDegrees: Int? = null,
        duration: Duration? = null
    ): MediaProperties {
        if (width <= 0 || height <= 0) return MediaProperties(duration)

        return when {
            (rotationDegrees ?: 0) % HALF_TURN_DEGREES != 0 -> MediaProperties(duration, width = height, height = width)
            else -> MediaProperties(duration, width, height)
        }
    }
}
