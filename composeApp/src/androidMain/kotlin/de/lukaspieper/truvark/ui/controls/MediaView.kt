/*
 * SPDX-FileCopyrightText: 2026 The Android Open Source Project
 * SPDX-FileCopyrightText: 2026 Lukas Pieper
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.lukaspieper.truvark.ui.controls

import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.compose.material3.buttons.PlayPauseButton
import androidx.media3.ui.compose.material3.buttons.PlaybackSpeedToggleButton
import androidx.media3.ui.compose.material3.buttons.RepeatButton
import androidx.media3.ui.compose.material3.buttons.SeekBackButton
import androidx.media3.ui.compose.material3.buttons.SeekForwardButton
import androidx.media3.ui.compose.material3.indicator.PositionAndDurationText
import androidx.media3.ui.compose.material3.indicator.ProgressSlider

@get:Composable
private val defaultIconButtonColors: IconButtonColors
    get() = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.primary)

@get:Composable
private val defaultTextButtonColors: ButtonColors
    get() = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)

@get:Composable
private val defaultTextColor: Color
    get() = MaterialTheme.colorScheme.primary

internal val customCenterControls: @Composable BoxScope.(player: Player?, showControls: Boolean) -> Unit =
    { player, showControls ->
        CenterControls(
            player,
            showControls,
            modifier = Modifier.fillMaxWidth(),
            innerModifier = Modifier
                .size(50.dp)
                .background(
                    MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                    ButtonDefaults.shape,
                ),
        )
    }

@OptIn(UnstableApi::class)
@Composable
private fun CenterControls(
    player: Player?,
    showControls: Boolean,
    modifier: Modifier = Modifier,
    innerModifier: Modifier = Modifier,
) {
    AnimatedVisibility(visible = showControls, enter = fadeIn(), exit = fadeOut()) {
        Row(
            modifier,
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SeekBackButton(player, innerModifier, colors = defaultIconButtonColors)
            PlayPauseButton(player, innerModifier, colors = defaultIconButtonColors)
            SeekForwardButton(player, innerModifier, colors = defaultIconButtonColors)
        }
    }
}

internal val customBottomControls: @Composable BoxScope.(player: Player?, showControls: Boolean) -> Unit =
    { player, showControls ->
        BottomControls(
            player,
            showControls,
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 15.dp)
                .safeDrawingPadding(),
        )
    }

@OptIn(UnstableApi::class)
@Composable
private fun BottomControls(player: Player?, showControls: Boolean, modifier: Modifier = Modifier) {
    AnimatedVisibility(visible = showControls, enter = fadeIn(), exit = fadeOut()) {
        Column(modifier) {
            ProgressSlider(player)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PositionAndDurationText(player, color = defaultTextColor)
                Spacer(Modifier.weight(1f))
                PlaybackSpeedToggleButton(player, colors = defaultTextButtonColors)
                RepeatButton(
                    player = player,
                    toggleModeSequence = listOf(Player.REPEAT_MODE_OFF, Player.REPEAT_MODE_ALL),
                    colors = defaultIconButtonColors
                )
            }
        }
    }
}
