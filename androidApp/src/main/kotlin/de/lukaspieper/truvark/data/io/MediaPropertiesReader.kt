/*
 * SPDX-FileCopyrightText: 2026 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.data.io

import android.content.Context
import android.graphics.BitmapFactory
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
import java.io.IOException
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
        val mimeType = fileInfo.mimeType

        return try {
            when {
                mimeType.startsWith("video/") || mimeType.startsWith("audio/") -> readMediaMetadata(uri)
                mimeType.startsWith("image/") -> readImageMetadata(uri)
                else -> MediaProperties()
            }
        } catch (e: Exception) {
            logcat(LogPriority.WARN) { e.asLog() }
            MediaProperties()
        }
    }

    private fun readMediaMetadata(uri: Uri): MediaProperties {
        return MediaMetadataRetriever().use { retriever ->
            retriever.setDataSource(context, uri)

            val duration = retriever.extractMetadata(METADATA_KEY_DURATION)
                ?.toLongOrNull()
                ?.takeIf { it >= 0L }
                ?.milliseconds

            createWithDimensions(
                width = retriever.extractMetadata(METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull() ?: 0,
                height = retriever.extractMetadata(METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull() ?: 0,
                rotationDegrees = retriever.extractMetadata(METADATA_KEY_VIDEO_ROTATION)?.toIntOrNull(),
                duration = duration
            )
        }
    }

    private fun readImageMetadata(uri: Uri): MediaProperties {
        val exif = try {
            context.contentResolver.openInputStream(uri)?.use { ExifInterface(it) }
        } catch (_: IOException) {
            null
        }

        val rotationDegrees = exif?.rotationDegrees
        val exifWidth = exif?.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, 0) ?: 0
        val exifHeight = exif?.getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH, 0) ?: 0

        if (exifWidth > 0 && exifHeight > 0) {
            return createWithDimensions(exifWidth, exifHeight, rotationDegrees)
        }

        logcat(LogPriority.DEBUG) { "EXIF dimensions unavailable; falling back to BitmapFactory." }

        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
        return createWithDimensions(options.outWidth, options.outHeight, rotationDegrees)
    }

    private fun createWithDimensions(
        width: Int,
        height: Int,
        rotationDegrees: Int? = null,
        duration: Duration? = null
    ): MediaProperties {
        if (width <= 0 || height <= 0) return MediaProperties(duration)

        val isQuarterTurn = (rotationDegrees ?: 0) % HALF_TURN_DEGREES != 0
        return when {
            isQuarterTurn -> MediaProperties(duration, width = height, height = width)
            else -> MediaProperties(duration, width, height)
        }
    }
}
