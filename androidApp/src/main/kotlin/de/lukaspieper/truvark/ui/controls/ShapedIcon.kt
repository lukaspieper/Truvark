/*
 * SPDX-FileCopyrightText: 2026 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.ui.controls

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.RoundedPolygon
import de.lukaspieper.truvark.R
import de.lukaspieper.truvark.ui.icons.MaterialSymbols
import de.lukaspieper.truvark.ui.preview.PreviewHost
import de.lukaspieper.truvark.ui.theme.paddings

@Composable
public fun ShapedImage(
    painter: Painter,
    background: Color,
    modifier: Modifier = Modifier,
    shape: RoundedPolygon = MaterialShapes.Circle
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(80.dp)
            .clip(shape.toShape())
            .background(background)
    ) {
        Image(
            painter = painter,
            contentDescription = null,
            modifier = Modifier.size(48.dp)
        )
    }
}

@Composable
public fun ShapedIcon(
    imageVector: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier,
    shape: RoundedPolygon = MaterialShapes.Circle,
    size: Dp = 48.dp
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .clip(shape.toShape())
            .background(tint.copy(alpha = 0.16f))
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(size / 2)
        )
    }
}

@Preview
@Composable
private fun ShapedIconPreview() = PreviewHost(Modifier) {
    Row(
        horizontalArrangement = spacedBy(MaterialTheme.paddings.small),
        modifier = Modifier.padding(MaterialTheme.paddings.medium)
    ) {
        ShapedIcon(
            imageVector = MaterialSymbols.Settings,
            tint = MaterialTheme.colorScheme.primary,
            shape = MaterialShapes.Sunny
        )

        ShapedImage(
            painter = painterResource(R.drawable.ic_launcher_foreground),
            background = MaterialTheme.colorScheme.primary,
            shape = MaterialShapes.Cookie12Sided
        )
    }
}
