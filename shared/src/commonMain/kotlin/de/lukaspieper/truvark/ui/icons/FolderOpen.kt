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
public val folder_open: ImageVector
    get() {
        if (_folder_open != null) {
            return _folder_open!!
        }
        _folder_open =
            ImageVector.Builder(
                name = "folder_open",
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
                        moveTo(4f, 20f)
                        quadTo(3.18f, 20f, 2.59f, 19.41f)
                        reflectiveQuadTo(2f, 18f)
                        verticalLineTo(6f)
                        quadTo(2f, 5.18f, 2.59f, 4.59f)
                        reflectiveQuadTo(4f, 4f)
                        horizontalLineToRelative(6f)
                        lineToRelative(2f, 2f)
                        horizontalLineToRelative(8f)
                        quadToRelative(0.83f, 0f, 1.41f, 0.59f)
                        quadTo(22f, 7.18f, 22f, 8f)
                        horizontalLineTo(11.18f)
                        lineToRelative(-2f, -2f)
                        horizontalLineTo(4f)
                        verticalLineTo(18f)
                        lineTo(6.4f, 10f)
                        horizontalLineTo(23.5f)
                        lineToRelative(-2.57f, 8.57f)
                        quadToRelative(-0.2f, 0.65f, -0.74f, 1.04f)
                        reflectiveQuadTo(19f, 20f)
                        horizontalLineTo(4f)
                        close()
                        moveTo(6.1f, 18f)
                        horizontalLineTo(19f)
                        lineToRelative(1.8f, -6f)
                        horizontalLineTo(7.9f)
                        lineTo(6.1f, 18f)
                        close()
                        moveToRelative(0f, 0f)
                        lineTo(7.9f, 12f)
                        lineTo(6.1f, 18f)
                        close()
                        moveTo(4f, 8f)
                        verticalLineTo(6f)
                        verticalLineTo(8f)
                        close()
                    }
                }
                .build()
        return _folder_open!!
    }

private var _folder_open: ImageVector? = null
