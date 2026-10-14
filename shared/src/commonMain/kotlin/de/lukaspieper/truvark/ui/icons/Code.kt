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
public val code: ImageVector
    get() {
        if (_code != null) {
            return _code!!
        }
        _code =
            ImageVector.Builder(
                name = "code",
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
                        moveTo(8f, 18f)
                        lineTo(2f, 12f)
                        lineTo(8f, 6f)
                        lineTo(9.43f, 7.43f)
                        lineToRelative(-4.6f, 4.6f)
                        lineTo(9.4f, 16.6f)
                        lineTo(8f, 18f)
                        close()
                        moveToRelative(8f, 0f)
                        lineTo(14.58f, 16.58f)
                        lineToRelative(4.6f, -4.6f)
                        lineTo(14.6f, 7.4f)
                        lineTo(16f, 6f)
                        lineToRelative(6f, 6f)
                        lineToRelative(-6f, 6f)
                        close()
                    }
                }
                .build()
        return _code!!
    }

private var _code: ImageVector? = null
