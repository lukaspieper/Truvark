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
public val view_list: ImageVector
    get() {
        if (_view_list != null) {
            return _view_list!!
        }
        _view_list =
            ImageVector.Builder(
                name = "view_list",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f,
                autoMirror = true,
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
                        moveTo(9f, 18f)
                        horizontalLineTo(20f)
                        verticalLineTo(15.33f)
                        horizontalLineTo(9f)
                        verticalLineTo(18f)
                        close()
                        moveTo(4f, 8.67f)
                        horizontalLineTo(7f)
                        verticalLineTo(6f)
                        horizontalLineTo(4f)
                        verticalLineTo(8.67f)
                        close()
                        moveToRelative(0f, 4.68f)
                        horizontalLineTo(7f)
                        verticalLineTo(10.68f)
                        horizontalLineTo(4f)
                        verticalLineToRelative(2.68f)
                        close()
                        moveTo(4f, 18f)
                        horizontalLineTo(7f)
                        verticalLineTo(15.33f)
                        horizontalLineTo(4f)
                        verticalLineTo(18f)
                        close()
                        moveTo(9f, 13.35f)
                        horizontalLineTo(20f)
                        verticalLineTo(10.68f)
                        horizontalLineTo(9f)
                        verticalLineToRelative(2.68f)
                        close()
                        moveTo(9f, 8.67f)
                        horizontalLineTo(20f)
                        verticalLineTo(6f)
                        horizontalLineTo(9f)
                        verticalLineTo(8.67f)
                        close()
                        moveTo(4f, 20f)
                        quadTo(3.18f, 20f, 2.59f, 19.41f)
                        reflectiveQuadTo(2f, 18f)
                        verticalLineTo(6f)
                        quadTo(2f, 5.18f, 2.59f, 4.59f)
                        reflectiveQuadTo(4f, 4f)
                        horizontalLineTo(20f)
                        quadToRelative(0.83f, 0f, 1.41f, 0.59f)
                        quadTo(22f, 5.18f, 22f, 6f)
                        verticalLineTo(18f)
                        quadToRelative(0f, 0.82f, -0.59f, 1.41f)
                        reflectiveQuadTo(20f, 20f)
                        horizontalLineTo(4f)
                        close()
                    }
                }
                .build()
        return _view_list!!
    }

private var _view_list: ImageVector? = null
