/*
 * SPDX-FileCopyrightText: 2022 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.ui.views.presenter.views

import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.media3.common.Player
import androidx.media3.common.Player.REPEAT_MODE_ALL
import androidx.media3.common.util.ExperimentalApi
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.ui.compose.material3.Player
import de.lukaspieper.truvark.ui.controls.customBottomControls
import de.lukaspieper.truvark.ui.controls.customCenterControls

@OptIn(UnstableApi::class, ExperimentalApi::class)
@Composable
public fun BoxScope.VideoContentView(
    mediaSource: MediaSource,
    isTopBarVisible: State<Boolean>,
    switchTopBarVisibility: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var player by remember { mutableStateOf<Player?>(null) }

    LifecycleStartEffect(Unit) {
        player = ExoPlayer.Builder(context)
            .setSeekBackIncrementMs(10_000)
            .setSeekForwardIncrementMs(10_000)
            .build()
            .apply {
                repeatMode = REPEAT_MODE_ALL
                setMediaSource(mediaSource)
                prepare()
            }

        onStopOrDispose {
            player?.apply { release() }
            player = null
        }
    }

    var centerX by remember { mutableFloatStateOf(0F) }
    player?.let { player ->
        // TODO: There is a full screen "flashing" when opening the second video onwards.
        Player(
            player = player,
            showControls = isTopBarVisible.value,
            topControls = null,
            centerControls = customCenterControls,
            bottomControls = customBottomControls,
            shutter = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                )
            },
            modifier = modifier
                .matchParentSize()
                .onSizeChanged { centerX = it.width / 2F }
                .simpleTapGesture(switchTopBarVisibility, player, centerX)
        )
    }
}

private fun Modifier.simpleTapGesture(
    switchTopBarVisibility: () -> Unit,
    player: Player,
    centerX: Float? = null
): Modifier {
    return this.pointerInput(centerX) {
        detectTapGestures(
            onDoubleTap = {
                if (centerX != null) {
                    when {
                        it.x > centerX -> player.seekForward()
                        else -> player.seekBack()
                    }
                }
            },
            onTap = {
                switchTopBarVisibility()
            }
        )
    }
}
