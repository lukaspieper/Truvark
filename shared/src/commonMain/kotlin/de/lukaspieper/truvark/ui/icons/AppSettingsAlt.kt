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
public val app_settings_alt: ImageVector
    get() {
        if (_app_settings_alt != null) {
            return _app_settings_alt!!
        }
        _app_settings_alt =
            ImageVector.Builder(
                name = "app_settings_alt",
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
                        moveTo(7f, 23f)
                        quadTo(6.18f, 23f, 5.59f, 22.41f)
                        reflectiveQuadTo(5f, 21f)
                        verticalLineTo(3f)
                        quadTo(5f, 2.17f, 5.59f, 1.59f)
                        reflectiveQuadTo(7f, 1f)
                        horizontalLineTo(17f)
                        quadToRelative(0.82f, 0f, 1.41f, 0.59f)
                        reflectiveQuadTo(19f, 3f)
                        verticalLineTo(6.1f)
                        quadToRelative(0.45f, 0.18f, 0.73f, 0.55f)
                        reflectiveQuadTo(20f, 7.5f)
                        verticalLineToRelative(2f)
                        quadToRelative(0f, 0.47f, -0.27f, 0.85f)
                        reflectiveQuadTo(19f, 10.9f)
                        verticalLineTo(21f)
                        quadToRelative(0f, 0.82f, -0.59f, 1.41f)
                        reflectiveQuadTo(17f, 23f)
                        horizontalLineTo(7f)
                        close()
                        moveTo(7f, 21f)
                        horizontalLineTo(17f)
                        verticalLineTo(3f)
                        horizontalLineTo(7f)
                        verticalLineTo(21f)
                        close()
                        moveToRelative(0f, 0f)
                        verticalLineTo(3f)
                        verticalLineTo(21f)
                        close()
                        moveToRelative(4f, -5f)
                        horizontalLineToRelative(2f)
                        lineToRelative(0.15f, -1.25f)
                        quadToRelative(0.2f, -0.08f, 0.35f, -0.16f)
                        reflectiveQuadToRelative(0.3f, -0.24f)
                        lineToRelative(1.15f, 0.5f)
                        lineToRelative(1f, -1.75f)
                        lineToRelative(-1f, -0.75f)
                        quadTo(15f, 12.15f, 15f, 11.98f)
                        reflectiveQuadTo(14.95f, 11.6f)
                        lineToRelative(1f, -0.75f)
                        lineTo(14.9f, 9.15f)
                        lineTo(13.8f, 9.6f)
                        quadTo(13.65f, 9.48f, 13.5f, 9.4f)
                        reflectiveQuadTo(13.15f, 9.25f)
                        lineTo(13f, 8f)
                        horizontalLineTo(11f)
                        lineTo(10.85f, 9.25f)
                        quadTo(10.65f, 9.32f, 10.5f, 9.4f)
                        quadTo(10.35f, 9.48f, 10.2f, 9.6f)
                        lineTo(9.1f, 9.15f)
                        lineToRelative(-1.05f, 1.7f)
                        lineToRelative(1f, 0.75f)
                        quadTo(9f, 11.8f, 9f, 11.98f)
                        reflectiveQuadToRelative(0.05f, 0.38f)
                        lineToRelative(-1f, 0.75f)
                        lineToRelative(1f, 1.75f)
                        lineToRelative(1.15f, -0.5f)
                        quadToRelative(0.15f, 0.15f, 0.3f, 0.24f)
                        reflectiveQuadToRelative(0.35f, 0.16f)
                        lineTo(11f, 16f)
                        close()
                        moveTo(10.93f, 13.08f)
                        quadTo(10.5f, 12.65f, 10.5f, 12f)
                        reflectiveQuadToRelative(0.43f, -1.08f)
                        reflectiveQuadTo(12f, 10.5f)
                        reflectiveQuadToRelative(1.08f, 0.42f)
                        reflectiveQuadTo(13.5f, 12f)
                        reflectiveQuadToRelative(-0.42f, 1.07f)
                        reflectiveQuadTo(12f, 13.5f)
                        reflectiveQuadTo(10.93f, 13.08f)
                        close()
                    }
                }
                .build()
        return _app_settings_alt!!
    }

private var _app_settings_alt: ImageVector? = null
