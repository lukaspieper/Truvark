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
public val manage_search: ImageVector
    get() {
        if (_manage_search != null) {
            return _manage_search!!
        }
        _manage_search =
            ImageVector.Builder(
                name = "manage_search",
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
                        moveTo(2f, 19f)
                        verticalLineTo(17f)
                        horizontalLineTo(12f)
                        verticalLineToRelative(2f)
                        horizontalLineTo(2f)
                        close()
                        moveTo(2f, 14f)
                        verticalLineTo(12f)
                        horizontalLineTo(7f)
                        verticalLineToRelative(2f)
                        horizontalLineTo(2f)
                        close()
                        moveTo(2f, 9f)
                        verticalLineTo(7f)
                        horizontalLineTo(7f)
                        verticalLineTo(9f)
                        horizontalLineTo(2f)
                        close()
                        moveTo(20.6f, 19f)
                        lineTo(16.75f, 15.15f)
                        quadToRelative(-0.6f, 0.42f, -1.31f, 0.64f)
                        quadTo(14.73f, 16f, 14f, 16f)
                        quadToRelative(-2.07f, 0f, -3.54f, -1.46f)
                        reflectiveQuadTo(9f, 11f)
                        quadTo(9f, 8.92f, 10.46f, 7.46f)
                        reflectiveQuadTo(14f, 6f)
                        reflectiveQuadToRelative(3.54f, 1.46f)
                        reflectiveQuadTo(19f, 11f)
                        quadToRelative(0f, 0.72f, -0.21f, 1.44f)
                        reflectiveQuadToRelative(-0.64f, 1.31f)
                        lineTo(22f, 17.6f)
                        lineTo(20.6f, 19f)
                        close()
                        moveTo(14f, 14f)
                        quadToRelative(1.25f, 0f, 2.13f, -0.88f)
                        reflectiveQuadTo(17f, 11f)
                        reflectiveQuadTo(16.13f, 8.88f)
                        reflectiveQuadTo(14f, 8f)
                        reflectiveQuadTo(11.88f, 8.88f)
                        reflectiveQuadTo(11f, 11f)
                        reflectiveQuadToRelative(0.88f, 2.13f)
                        reflectiveQuadTo(14f, 14f)
                        close()
                    }
                }
                .build()
        return _manage_search!!
    }

private var _manage_search: ImageVector? = null
