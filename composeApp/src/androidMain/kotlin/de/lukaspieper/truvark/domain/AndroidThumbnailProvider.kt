/*
 * SPDX-FileCopyrightText: 2022 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.domain

import android.content.Context
import android.graphics.Bitmap
import coil3.ImageLoader
import coil3.imageDecoderEnabled
import coil3.request.CachePolicy
import coil3.request.ErrorResult
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowConversionToBitmap
import coil3.toBitmap
import coil3.video.videoFramePercent
import de.lukaspieper.truvark.data.io.FileInfo
import logcat.LogPriority
import logcat.asLog
import logcat.logcat
import java.io.ByteArrayOutputStream

public class AndroidThumbnailProvider(private val context: Context) : ThumbnailProvider {

    public companion object {
        public const val THUMBNAIL_QUALITY: Int = 50
        public const val THUMBNAIL_SIZE: Int = 280
    }

    private val imageLoader = ImageLoader.Builder(context)
        .diskCachePolicy(CachePolicy.DISABLED)
        .memoryCachePolicy(CachePolicy.DISABLED)
        // https://github.com/coil-kt/coil/issues/2808
        // for some reason the quality decreases is only noticeable for thumbnail generation (not AsyncImage).
        .imageDecoderEnabled(false)
        .build()

    override suspend fun createThumbnail(file: FileInfo): ByteArray? {
        try {
            val thumbnail = createThumbnailFromFile(file)
            return compressBitmapToByteArray(thumbnail)
        } catch (e: Exception) {
            logcat(LogPriority.WARN) { e.asLog() }
        }

        return null
    }

    private suspend fun createThumbnailFromFile(file: FileInfo): Bitmap {
        val request = ImageRequest.Builder(context)
            .data(file.uri)
            .allowConversionToBitmap(true)
            .size(THUMBNAIL_SIZE, THUMBNAIL_SIZE)
            .videoFramePercent(0.07)
            .build()

        val result = imageLoader.execute(request)
        when (result) {
            is SuccessResult -> return result.image.toBitmap()
            is ErrorResult -> throw result.throwable
        }
    }

    private fun compressBitmapToByteArray(thumbnail: Bitmap): ByteArray {
        ByteArrayOutputStream().use { byteArrayBitmapStream ->
            thumbnail.compress(Bitmap.CompressFormat.WEBP, THUMBNAIL_QUALITY, byteArrayBitmapStream)
            return byteArrayBitmapStream.toByteArray()
        }
    }
}
