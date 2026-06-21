/*
 * SPDX-FileCopyrightText: 2026 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.domain.crypto.decryption.coil

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import coil3.Extras
import coil3.asImage
import coil3.decode.DataSource
import coil3.disk.DiskCache
import coil3.disk.directory
import coil3.getExtra
import coil3.intercept.Interceptor
import coil3.request.ErrorResult
import coil3.request.ImageRequest
import coil3.request.ImageResult
import coil3.request.SuccessResult
import coil3.toBitmap
import de.lukaspieper.truvark.data.io.FileInfo
import de.lukaspieper.truvark.domain.vault.Vault
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import logcat.LogPriority
import logcat.asLog
import logcat.logcat
import okio.buffer
import java.io.File
import kotlin.uuid.Uuid

internal class ThumbnailCacheInterceptor(
    private val vault: Vault,
    diskCacheDirectory: File
) : Interceptor {

    private val diskCache = DiskCache.Builder()
        .directory(diskCacheDirectory.resolve("encrypted"))
        .build()

    /**
     * A common object to set as [FileInfo.uri] when trying to get a thumbnail cache hit without the real uri.
     */
    object ThumbnailCacheUri

    companion object {
        private val UseThumbnailCacheExtra = Extras.Key(false)

        fun ImageRequest.Builder.useThumbnailCache(enabled: Boolean) = apply {
            extras[UseThumbnailCacheExtra] = enabled
        }

        val ImageRequest.useThumbnailCache: Boolean
            get() = getExtra(UseThumbnailCacheExtra)
    }

    override suspend fun intercept(chain: Interceptor.Chain): ImageResult {
        val data = chain.request.data

        if (data !is FileInfo || !chain.request.useThumbnailCache) {
            return chain.proceed()
        }

        return withContext(Dispatchers.IO) {
            val cacheKey = Uuid.parseHexOrNull(data.fullName)?.let { uuid ->
                vault.prfSet.computePrimary(uuid.toByteArray(), 32)
            }

            if (cacheKey != null) {
                readFromDiskCache(cacheKey)?.let { bitmap ->
                    return@withContext SuccessResult(
                        image = bitmap.asImage(),
                        request = chain.request,
                        dataSource = DataSource.DISK,
                    )
                }
            }

            if (data.uri is ThumbnailCacheUri) {
                return@withContext ErrorResult(
                    image = null,
                    request = chain.request,
                    throwable = IllegalStateException("Thumbnail cache miss on thumbnail-only request.")
                )
            }

            val result = chain.proceed()

            if (cacheKey != null && result is SuccessResult) {
                val bitmap = writeToDiskCache(cacheKey, result)

                // Returning the bitmap here to avoid animated thumbnails (e.g. GIFs) on cache misses.
                bitmap?.let {
                    return@withContext SuccessResult(it.asImage(), chain.request, DataSource.DISK)
                }
            }

            return@withContext result
        }
    }

    private fun readFromDiskCache(cacheKey: ByteArray): Bitmap? {
        try {
            return diskCache.openSnapshot(cacheKey.toHexString())?.use { snapshot ->
                diskCache.fileSystem.source(snapshot.data).buffer().inputStream().use { inputStream ->
                    vault.streamingAead.newDecryptingStream(inputStream, cacheKey)
                        .use { decryptingInputStream ->
                            BitmapFactory.decodeStream(decryptingInputStream)
                        }
                }
            }
        } catch (e: Exception) {
            logcat(LogPriority.WARN) { e.asLog() }
            return null
        }
    }

    private fun writeToDiskCache(cacheKey: ByteArray, result: SuccessResult): Bitmap? {
        val editor = diskCache.openEditor(cacheKey.toHexString()) ?: return null

        try {
            val bitmap = result.image.toBitmap()

            diskCache.fileSystem.sink(editor.data).buffer().outputStream().use { outputStream ->
                vault.streamingAead.newEncryptingStream(outputStream, cacheKey)
                    .use { encryptingOutputStream ->
                        bitmap.compress(Bitmap.CompressFormat.WEBP_LOSSY, 80, encryptingOutputStream)
                    }
            }

            editor.commit()
            return bitmap
        } catch (e: Exception) {
            logcat(LogPriority.WARN) { e.asLog() }
            editor.abort()
        }

        return null
    }
}
