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
public val copyright: ImageVector
    get() {
        if (_copyright != null) {
            return _copyright!!
        }
        _copyright =
            ImageVector.Builder(
                name = "copyright",
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
                        moveTo(10f, 16f)
                        horizontalLineToRelative(4f)
                        quadToRelative(0.43f, 0f, 0.71f, -0.29f)
                        reflectiveQuadTo(15f, 15f)
                        verticalLineTo(13f)
                        horizontalLineTo(13f)
                        verticalLineToRelative(1f)
                        horizontalLineTo(11f)
                        verticalLineTo(10f)
                        horizontalLineToRelative(2f)
                        verticalLineToRelative(1f)
                        horizontalLineToRelative(2f)
                        verticalLineTo(9f)
                        quadTo(15f, 8.57f, 14.71f, 8.29f)
                        reflectiveQuadTo(14f, 8f)
                        horizontalLineTo(10f)
                        quadTo(9.58f, 8f, 9.29f, 8.29f)
                        reflectiveQuadTo(9f, 9f)
                        verticalLineToRelative(6f)
                        quadToRelative(0f, 0.42f, 0.29f, 0.71f)
                        quadTo(9.58f, 16f, 10f, 16f)
                        close()
                        moveToRelative(2f, 6f)
                        quadTo(9.93f, 22f, 8.1f, 21.21f)
                        quadTo(6.28f, 20.43f, 4.93f, 19.08f)
                        quadTo(3.58f, 17.73f, 2.79f, 15.9f)
                        reflectiveQuadTo(2f, 12f)
                        quadTo(2f, 9.92f, 2.79f, 8.1f)
                        quadTo(3.58f, 6.27f, 4.93f, 4.93f)
                        quadTo(6.28f, 3.57f, 8.1f, 2.79f)
                        quadTo(9.93f, 2f, 12f, 2f)
                        reflectiveQuadToRelative(3.9f, 0.79f)
                        reflectiveQuadToRelative(3.17f, 2.14f)
                        quadToRelative(1.35f, 1.35f, 2.14f, 3.17f)
                        quadTo(22f, 9.92f, 22f, 12f)
                        reflectiveQuadToRelative(-0.79f, 3.9f)
                        reflectiveQuadToRelative(-2.14f, 3.17f)
                        quadToRelative(-1.35f, 1.35f, -3.17f, 2.14f)
                        reflectiveQuadTo(12f, 22f)
                        close()
                        moveToRelative(0f, -2f)
                        quadToRelative(3.35f, 0f, 5.68f, -2.32f)
                        reflectiveQuadTo(20f, 12f)
                        reflectiveQuadTo(17.68f, 6.32f)
                        reflectiveQuadTo(12f, 4f)
                        reflectiveQuadTo(6.33f, 6.32f)
                        reflectiveQuadTo(4f, 12f)
                        reflectiveQuadToRelative(2.33f, 5.68f)
                        reflectiveQuadTo(12f, 20f)
                        close()
                        moveToRelative(0f, -8f)
                        close()
                    }
                }
                .build()
        return _copyright!!
    }

private var _copyright: ImageVector? = null
