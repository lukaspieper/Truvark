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
public val select_all: ImageVector
    get() {
        if (_select_all != null) {
            return _select_all!!
        }
        _select_all =
            ImageVector.Builder(
                name = "select_all",
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
                        moveTo(7f, 17f)
                        verticalLineTo(7f)
                        horizontalLineTo(17f)
                        verticalLineTo(17f)
                        horizontalLineTo(7f)
                        close()
                        moveTo(9f, 15f)
                        horizontalLineToRelative(6f)
                        verticalLineTo(9f)
                        horizontalLineTo(9f)
                        verticalLineToRelative(6f)
                        close()
                        moveTo(5f, 19f)
                        verticalLineToRelative(2f)
                        quadTo(4.18f, 21f, 3.59f, 20.41f)
                        reflectiveQuadTo(3f, 19f)
                        horizontalLineTo(5f)
                        close()
                        moveTo(3f, 17f)
                        verticalLineTo(15f)
                        horizontalLineTo(5f)
                        verticalLineToRelative(2f)
                        horizontalLineTo(3f)
                        close()
                        moveTo(3f, 13f)
                        verticalLineTo(11f)
                        horizontalLineTo(5f)
                        verticalLineToRelative(2f)
                        horizontalLineTo(3f)
                        close()
                        moveTo(3f, 9f)
                        verticalLineTo(7f)
                        horizontalLineTo(5f)
                        verticalLineTo(9f)
                        horizontalLineTo(3f)
                        close()
                        moveTo(5f, 5f)
                        horizontalLineTo(3f)
                        quadTo(3f, 4.17f, 3.59f, 3.59f)
                        reflectiveQuadTo(5f, 3f)
                        verticalLineTo(5f)
                        close()
                        moveTo(7f, 21f)
                        verticalLineTo(19f)
                        horizontalLineTo(9f)
                        verticalLineToRelative(2f)
                        horizontalLineTo(7f)
                        close()
                        moveTo(7f, 5f)
                        verticalLineTo(3f)
                        horizontalLineTo(9f)
                        verticalLineTo(5f)
                        horizontalLineTo(7f)
                        close()
                        moveToRelative(4f, 16f)
                        verticalLineTo(19f)
                        horizontalLineToRelative(2f)
                        verticalLineToRelative(2f)
                        horizontalLineTo(11f)
                        close()
                        moveTo(11f, 5f)
                        verticalLineTo(3f)
                        horizontalLineToRelative(2f)
                        verticalLineTo(5f)
                        horizontalLineTo(11f)
                        close()
                        moveToRelative(4f, 16f)
                        verticalLineTo(19f)
                        horizontalLineToRelative(2f)
                        verticalLineToRelative(2f)
                        horizontalLineTo(15f)
                        close()
                        moveTo(15f, 5f)
                        verticalLineTo(3f)
                        horizontalLineToRelative(2f)
                        verticalLineTo(5f)
                        horizontalLineTo(15f)
                        close()
                        moveToRelative(4f, 16f)
                        verticalLineTo(19f)
                        horizontalLineToRelative(2f)
                        quadToRelative(0f, 0.82f, -0.59f, 1.41f)
                        reflectiveQuadTo(19f, 21f)
                        close()
                        moveToRelative(0f, -4f)
                        verticalLineTo(15f)
                        horizontalLineToRelative(2f)
                        verticalLineToRelative(2f)
                        horizontalLineTo(19f)
                        close()
                        moveToRelative(0f, -4f)
                        verticalLineTo(11f)
                        horizontalLineToRelative(2f)
                        verticalLineToRelative(2f)
                        horizontalLineTo(19f)
                        close()
                        moveTo(19f, 9f)
                        verticalLineTo(7f)
                        horizontalLineToRelative(2f)
                        verticalLineTo(9f)
                        horizontalLineTo(19f)
                        close()
                        moveTo(19f, 5f)
                        verticalLineTo(3f)
                        quadToRelative(0.83f, 0f, 1.41f, 0.59f)
                        reflectiveQuadTo(21f, 5f)
                        horizontalLineTo(19f)
                        close()
                    }
                }
                .build()
        return _select_all!!
    }

private var _select_all: ImageVector? = null
