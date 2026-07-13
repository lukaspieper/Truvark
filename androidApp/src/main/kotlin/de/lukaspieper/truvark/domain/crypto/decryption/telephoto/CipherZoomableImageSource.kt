/*
 * SPDX-FileCopyrightText: 2023 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.domain.crypto.decryption.telephoto

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalContext
import coil3.BitmapImage
import coil3.ImageLoader
import coil3.compose.asPainter
import coil3.request.ImageRequest
import coil3.request.ImageResult
import coil3.request.SuccessResult
import coil3.request.maxBitmapSize
import coil3.size.Dimension
import de.lukaspieper.truvark.data.io.FileInfo
import de.lukaspieper.truvark.domain.vault.Vault
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import me.saket.telephoto.subsamplingimage.ImageBitmapOptions
import me.saket.telephoto.subsamplingimage.SubSamplingImageSource
import me.saket.telephoto.subsamplingimage.util.canBeSubSampled
import me.saket.telephoto.zoomable.ZoomableImageSource
import me.saket.telephoto.zoomable.ZoomableImageSource.ResolveResult
import me.saket.telephoto.zoomable.copy
import kotlin.math.roundToInt
import coil3.size.Size as CoilSize

/**
 * A [ZoomableImageSource] for Telephoto to load encrypted images with support for subsampling while keeping fallback to
 * Coil for other image types like GIFs, SVGs, etc.
 */
@Immutable
internal class CipherZoomableImageSource(
    private val model: FileInfo,
    private val imageLoader: ImageLoader,
    private val vault: Vault,
) : ZoomableImageSource {

    @Composable
    override fun resolve(canvasSize: Flow<Size>): ResolveResult {
        val context = LocalContext.current
        val resolver = remember(this) {
            Resolver(
                request = ImageRequest.Builder(context)
                    .data(model)
                    .size { canvasSize.first().toCoilSize() }
                    .maxBitmapSize(CoilSize.ORIGINAL)
                    .build(),
                imageLoader = imageLoader,
                vault = vault
            )
        }
        return resolver.resolved
    }

    private fun Size.toCoilSize() = CoilSize(
        width = if (width.isFinite()) Dimension(width.roundToInt()) else Dimension.Undefined,
        height = if (height.isFinite()) Dimension(height.roundToInt()) else Dimension.Undefined
    )
}

private class Resolver(
    private val request: ImageRequest,
    private val imageLoader: ImageLoader,
    private val vault: Vault,
) : RememberWorker() {
    var resolved: ResolveResult by mutableStateOf(
        ResolveResult(delegate = null)
    )

    override suspend fun work() {
        val result = imageLoader.execute(request)
        if (result !is SuccessResult) return

        val imageSource = when (result.image) {
            is BitmapImage -> result.toSubSamplingImageSource()
            else -> null
        }

        resolved = resolved.copy(
            delegate = if (imageSource != null) {
                ZoomableImageSource.SubSamplingDelegate(
                    source = imageSource,
                    imageOptions = ImageBitmapOptions(from = (result.image as BitmapImage).bitmap)
                )
            } else {
                ZoomableImageSource.PainterDelegate(
                    painter = result.image.asPainter(request.context)
                )
            }
        )
    }

    private suspend fun ImageResult.toSubSamplingImageSource(): SubSamplingImageSource? {
        val source = when (request.data) {
            is FileInfo -> {
                val decryptingFileHandle = vault.createDecryptingFileHandle(request.data as FileInfo)

                SubSamplingImageSource.rawSource(
                    source = { decryptingFileHandle.source() },
                    onClose = { decryptingFileHandle.close() }
                )
            }

            else -> null
        }

        return if (source != null && source.canBeSubSampled(request.context)) {
            source
        } else {
            source?.close()
            null
        }
    }
}
