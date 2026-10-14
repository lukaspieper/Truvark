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
public val file_open: ImageVector
    get() {
        if (_file_open != null) {
            return _file_open!!
        }
        _file_open =
            ImageVector.Builder(
                name = "file_open",
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
                        moveTo(6f, 22f)
                        quadTo(5.18f, 22f, 4.59f, 21.41f)
                        reflectiveQuadTo(4f, 20f)
                        verticalLineTo(4f)
                        quadTo(4f, 3.17f, 4.59f, 2.59f)
                        reflectiveQuadTo(6f, 2f)
                        horizontalLineToRelative(8f)
                        lineToRelative(6f, 6f)
                        verticalLineToRelative(6f)
                        horizontalLineTo(18f)
                        verticalLineTo(9f)
                        horizontalLineTo(13f)
                        verticalLineTo(4f)
                        horizontalLineTo(6f)
                        verticalLineTo(20f)
                        horizontalLineToRelative(9f)
                        verticalLineToRelative(2f)
                        horizontalLineTo(6f)
                        close()
                        moveToRelative(15.95f, 0.38f)
                        lineTo(19f, 19.43f)
                        verticalLineToRelative(2.22f)
                        horizontalLineTo(17f)
                        verticalLineTo(16f)
                        horizontalLineToRelative(5.65f)
                        verticalLineToRelative(2f)
                        horizontalLineTo(20.4f)
                        lineToRelative(2.95f, 2.95f)
                        lineToRelative(-1.4f, 1.43f)
                        close()
                        moveTo(6f, 20f)
                        verticalLineTo(14f)
                        verticalLineTo(9f)
                        verticalLineTo(4f)
                        verticalLineTo(20f)
                        close()
                    }
                }
                .build()
        return _file_open!!
    }

private var _file_open: ImageVector? = null
