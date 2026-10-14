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
public val cloud_upload: ImageVector
    get() {
        if (_cloud_upload != null) {
            return _cloud_upload!!
        }
        _cloud_upload =
            ImageVector.Builder(
                name = "cloud_upload",
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
                        moveTo(6.5f, 20f)
                        quadTo(4.23f, 20f, 2.61f, 18.43f)
                        reflectiveQuadTo(1f, 14.58f)
                        quadTo(1f, 12.63f, 2.18f, 11.1f)
                        reflectiveQuadTo(5.25f, 9.15f)
                        quadTo(5.88f, 6.85f, 7.75f, 5.43f)
                        reflectiveQuadTo(12f, 4f)
                        quadToRelative(2.93f, 0f, 4.96f, 2.04f)
                        reflectiveQuadTo(19f, 11f)
                        quadToRelative(1.73f, 0.2f, 2.86f, 1.49f)
                        reflectiveQuadTo(23f, 15.5f)
                        quadToRelative(0f, 1.88f, -1.31f, 3.19f)
                        reflectiveQuadTo(18.5f, 20f)
                        horizontalLineTo(13f)
                        quadToRelative(-0.82f, 0f, -1.41f, -0.59f)
                        reflectiveQuadTo(11f, 18f)
                        verticalLineTo(12.85f)
                        lineTo(9.4f, 14.4f)
                        lineTo(8f, 13f)
                        lineTo(12f, 9f)
                        lineToRelative(4f, 4f)
                        lineToRelative(-1.4f, 1.4f)
                        lineTo(13f, 12.85f)
                        verticalLineTo(18f)
                        horizontalLineToRelative(5.5f)
                        quadToRelative(1.05f, 0f, 1.78f, -0.73f)
                        reflectiveQuadTo(21f, 15.5f)
                        reflectiveQuadTo(20.28f, 13.73f)
                        reflectiveQuadTo(18.5f, 13f)
                        horizontalLineTo(17f)
                        verticalLineTo(11f)
                        quadTo(17f, 8.92f, 15.54f, 7.46f)
                        reflectiveQuadTo(12f, 6f)
                        quadTo(9.93f, 6f, 8.46f, 7.46f)
                        reflectiveQuadTo(7f, 11f)
                        horizontalLineTo(6.5f)
                        quadTo(5.05f, 11f, 4.03f, 12.02f)
                        reflectiveQuadTo(3f, 14.5f)
                        reflectiveQuadToRelative(1.03f, 2.48f)
                        reflectiveQuadTo(6.5f, 18f)
                        horizontalLineTo(9f)
                        verticalLineToRelative(2f)
                        horizontalLineTo(6.5f)
                        close()
                        moveTo(12f, 13f)
                        close()
                    }
                }
                .build()
        return _cloud_upload!!
    }

private var _cloud_upload: ImageVector? = null
