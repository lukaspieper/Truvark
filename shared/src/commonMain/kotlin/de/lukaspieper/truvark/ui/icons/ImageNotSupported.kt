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
public val image_not_supported: ImageVector
    get() {
        if (_image_not_supported != null) {
            return _image_not_supported!!
        }
        _image_not_supported =
            ImageVector.Builder(
                name = "image_not_supported",
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
                        moveTo(20.48f, 23.3f)
                        lineTo(18.18f, 21f)
                        horizontalLineTo(5f)
                        quadTo(4.18f, 21f, 3.59f, 20.41f)
                        reflectiveQuadTo(3f, 19f)
                        verticalLineTo(5.82f)
                        lineTo(0.7f, 3.5f)
                        lineTo(2.1f, 2.1f)
                        lineTo(21.9f, 21.9f)
                        lineToRelative(-1.43f, 1.4f)
                        close()
                        moveTo(5f, 19f)
                        horizontalLineTo(16.18f)
                        lineToRelative(-2f, -2f)
                        horizontalLineTo(6f)
                        lineTo(9f, 13f)
                        lineToRelative(2f, 2.72f)
                        lineToRelative(0.85f, -1.05f)
                        lineTo(5f, 7.82f)
                        verticalLineTo(19f)
                        close()
                        moveTo(21f, 18.18f)
                        lineToRelative(-2f, -2f)
                        verticalLineTo(5f)
                        horizontalLineTo(7.83f)
                        lineToRelative(-2f, -2f)
                        horizontalLineTo(19f)
                        quadToRelative(0.83f, 0f, 1.41f, 0.59f)
                        reflectiveQuadTo(21f, 5f)
                        verticalLineTo(18.18f)
                        close()
                        moveTo(13.48f, 10.65f)
                        close()
                        moveTo(10.6f, 13.43f)
                        close()
                    }
                }
                .build()
        return _image_not_supported!!
    }

private var _image_not_supported: ImageVector? = null
