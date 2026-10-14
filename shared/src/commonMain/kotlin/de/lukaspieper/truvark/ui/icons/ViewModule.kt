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
public val view_module: ImageVector
    get() {
        if (_view_module != null) {
            return _view_module!!
        }
        _view_module =
            ImageVector.Builder(
                name = "view_module",
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
                        moveTo(15.68f, 11f)
                        horizontalLineTo(19f)
                        verticalLineTo(7f)
                        horizontalLineTo(15.68f)
                        verticalLineToRelative(4f)
                        close()
                        moveToRelative(-5.35f, 0f)
                        horizontalLineToRelative(3.32f)
                        verticalLineTo(7f)
                        horizontalLineTo(10.33f)
                        verticalLineToRelative(4f)
                        close()
                        moveTo(5f, 11f)
                        horizontalLineTo(8.33f)
                        verticalLineTo(7f)
                        horizontalLineTo(5f)
                        verticalLineToRelative(4f)
                        close()
                        moveToRelative(0f, 6f)
                        horizontalLineTo(8.33f)
                        verticalLineTo(13f)
                        horizontalLineTo(5f)
                        verticalLineToRelative(4f)
                        close()
                        moveToRelative(5.33f, 0f)
                        horizontalLineToRelative(3.32f)
                        verticalLineTo(13f)
                        horizontalLineTo(10.33f)
                        verticalLineToRelative(4f)
                        close()
                        moveToRelative(5.35f, 0f)
                        horizontalLineTo(19f)
                        verticalLineTo(13f)
                        horizontalLineTo(15.68f)
                        verticalLineToRelative(4f)
                        close()
                        moveTo(3f, 17f)
                        verticalLineTo(7f)
                        quadTo(3f, 6.18f, 3.59f, 5.59f)
                        reflectiveQuadTo(5f, 5f)
                        horizontalLineTo(19f)
                        quadToRelative(0.83f, 0f, 1.41f, 0.59f)
                        quadTo(21f, 6.18f, 21f, 7f)
                        verticalLineTo(17f)
                        quadToRelative(0f, 0.82f, -0.59f, 1.41f)
                        reflectiveQuadTo(19f, 19f)
                        horizontalLineTo(5f)
                        quadTo(4.18f, 19f, 3.59f, 18.41f)
                        reflectiveQuadTo(3f, 17f)
                        close()
                    }
                }
                .build()
        return _view_module!!
    }

private var _view_module: ImageVector? = null
