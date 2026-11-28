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
public val text_fields: ImageVector
    get() {
        if (cachedTextFields != null) {
            return cachedTextFields!!
        }
        cachedTextFields =
            ImageVector.Builder(
                name = "text_fields",
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
                        pathFillType = PathFillType.NonZero,
                    ) {
                        moveTo(2f, 4f)
                        verticalLineTo(7f)
                        horizontalLineTo(7f)
                        verticalLineTo(20f)
                        horizontalLineTo(10f)
                        verticalLineTo(7f)
                        horizontalLineTo(15f)
                        verticalLineTo(4f)
                        horizontalLineTo(2f)
                        close()
                        moveTo(22f, 11f)
                        horizontalLineTo(14f)
                        verticalLineTo(14f)
                        horizontalLineTo(16.5f)
                        verticalLineTo(20f)
                        horizontalLineTo(19.5f)
                        verticalLineTo(14f)
                        horizontalLineTo(22f)
                        verticalLineTo(11f)
                        close()
                    }
                }
                .build()
        return cachedTextFields!!
    }

private var cachedTextFields: ImageVector? = null
