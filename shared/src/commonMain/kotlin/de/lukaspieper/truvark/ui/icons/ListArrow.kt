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
public val list_arrow: ImageVector
    get() {
        if (cachedListArrow != null) {
            return cachedListArrow!!
        }
        cachedListArrow =
            ImageVector.Builder(
                name = "list_arrow",
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
                        moveTo(6f, 20f)
                        lineTo(2f, 16f)
                        lineTo(3.4f, 14.6f)
                        lineTo(5f, 16.15f)
                        verticalLineTo(5f)
                        horizontalLineTo(7f)
                        verticalLineTo(16.15f)
                        lineTo(8.6f, 14.6f)
                        lineTo(10f, 16f)
                        lineTo(6f, 20f)
                        close()
                        moveToRelative(6f, -1f)
                        verticalLineTo(17f)
                        horizontalLineTo(22f)
                        verticalLineTo(19f)
                        horizontalLineTo(12f)
                        close()
                        moveToRelative(0f, -6f)
                        verticalLineTo(11f)
                        horizontalLineTo(22f)
                        verticalLineTo(13f)
                        horizontalLineTo(12f)
                        close()
                        moveTo(12f, 7f)
                        verticalLineTo(5f)
                        horizontalLineTo(22f)
                        verticalLineTo(7f)
                        horizontalLineTo(12f)
                        close()
                    }
                }
                .build()
        return cachedListArrow!!
    }

private var cachedListArrow: ImageVector? = null
