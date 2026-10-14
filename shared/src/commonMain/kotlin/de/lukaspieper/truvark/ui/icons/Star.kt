/*
 * SPDX-FileCopyrightText: Google LLC
 * SPDX-FileCopyrightText: 2026 Lukas Pieper
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.lukaspieper.truvark.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

@Suppress("CheckReturnValue")
public val star: ImageVector
    get() {
        if (_star != null) {
            return _star!!
        }
        _star =
            ImageVector.Builder(
                name = "star",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f,
            )
                .apply {
                    path(
                        fill = SolidColor(Color.Black),
                        fillAlpha = 1f,
                        stroke = null,
                        strokeAlpha = 1f,
                        strokeLineWidth = 1f,
                        strokeLineCap = StrokeCap.Butt,
                        strokeLineJoin = StrokeJoin.Bevel,
                        strokeLineMiter = 1f,
                        pathFillType = PathFillType.Companion.NonZero,
                    ) {
                        moveTo(8.85f, 16.83f)
                        lineTo(12f, 14.93f)
                        lineToRelative(3.15f, 1.93f)
                        lineToRelative(-0.82f, -3.6f)
                        lineToRelative(2.78f, -2.4f)
                        lineTo(13.45f, 10.52f)
                        lineTo(12f, 7.13f)
                        lineTo(10.55f, 10.5f)
                        lineTo(6.9f, 10.83f)
                        lineToRelative(2.78f, 2.43f)
                        lineTo(8.85f, 16.83f)
                        close()
                        moveTo(5.83f, 21f)
                        lineTo(7.45f, 13.98f)
                        lineTo(2f, 9.25f)
                        lineTo(9.2f, 8.63f)
                        lineTo(12f, 2f)
                        lineToRelative(2.8f, 6.63f)
                        lineTo(22f, 9.25f)
                        lineToRelative(-5.45f, 4.72f)
                        lineTo(18.18f, 21f)
                        lineTo(12f, 17.27f)
                        lineTo(5.83f, 21f)
                        close()
                        moveTo(12f, 12.25f)
                        close()
                    }
                }
                .build()
        return _star!!
    }

private var _star: ImageVector? = null
