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
public val calendar_today: ImageVector
    get() {
        if (cachedCalendarToday != null) {
            return cachedCalendarToday!!
        }
        cachedCalendarToday =
            ImageVector.Builder(
                name = "calendar_today",
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
                        pathFillType = PathFillType.NonZero,
                    ) {
                        moveTo(19f, 4f)
                        horizontalLineTo(18f)
                        verticalLineTo(2f)
                        horizontalLineTo(16f)
                        verticalLineTo(4f)
                        horizontalLineTo(8f)
                        verticalLineTo(2f)
                        horizontalLineTo(6f)
                        verticalLineTo(4f)
                        horizontalLineTo(5f)
                        curveTo(3.89f, 4f, 3.01f, 4.9f, 3.01f, 6f)
                        lineTo(3f, 20f)
                        curveTo(3f, 21.1f, 3.89f, 22f, 5f, 22f)
                        horizontalLineTo(19f)
                        curveTo(20.1f, 22f, 21f, 21.1f, 21f, 20f)
                        verticalLineTo(6f)
                        curveTo(21f, 4.9f, 20.1f, 4f, 19f, 4f)
                        close()
                        moveTo(19f, 20f)
                        horizontalLineTo(5f)
                        verticalLineTo(9f)
                        horizontalLineTo(19f)
                        verticalLineTo(20f)
                        close()
                    }
                }
                .build()
        return cachedCalendarToday!!
    }

private var cachedCalendarToday: ImageVector? = null
