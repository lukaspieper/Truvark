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
public val fingerprint: ImageVector
    get() {
        if (_fingerprint != null) {
            return _fingerprint!!
        }
        _fingerprint =
            ImageVector.Builder(
                name = "fingerprint",
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
                        moveTo(12.03f, 4.47f)
                        quadToRelative(2.65f, 0f, 5f, 1.14f)
                        reflectiveQuadTo(20.95f, 8.9f)
                        quadToRelative(0.17f, 0.23f, 0.11f, 0.4f)
                        quadTo(21f, 9.48f, 20.85f, 9.6f)
                        reflectiveQuadTo(20.5f, 9.71f)
                        reflectiveQuadTo(20.15f, 9.5f)
                        quadTo(18.78f, 7.55f, 16.61f, 6.51f)
                        reflectiveQuadTo(12.03f, 5.47f)
                        reflectiveQuadTo(7.48f, 6.51f)
                        reflectiveQuadTo(3.95f, 9.5f)
                        quadTo(3.8f, 9.73f, 3.6f, 9.75f)
                        quadTo(3.4f, 9.77f, 3.25f, 9.65f)
                        quadTo(3.08f, 9.52f, 3.04f, 9.34f)
                        reflectiveQuadTo(3.15f, 8.95f)
                        quadTo(4.7f, 6.82f, 7.04f, 5.65f)
                        reflectiveQuadTo(12.03f, 4.47f)
                        close()
                        moveToRelative(0f, 2.35f)
                        quadToRelative(3.38f, 0f, 5.8f, 2.25f)
                        reflectiveQuadToRelative(2.43f, 5.58f)
                        quadToRelative(0f, 1.25f, -0.89f, 2.09f)
                        reflectiveQuadTo(17.2f, 17.58f)
                        quadToRelative(-1.27f, 0f, -2.19f, -0.84f)
                        reflectiveQuadTo(14.1f, 14.65f)
                        quadToRelative(0f, -0.83f, -0.61f, -1.39f)
                        reflectiveQuadTo(12.03f, 12.7f)
                        reflectiveQuadToRelative(-1.46f, 0.56f)
                        reflectiveQuadTo(9.95f, 14.65f)
                        quadToRelative(0f, 2.42f, 1.44f, 4.05f)
                        reflectiveQuadToRelative(3.71f, 2.28f)
                        quadToRelative(0.22f, 0.07f, 0.3f, 0.25f)
                        reflectiveQuadToRelative(0.03f, 0.38f)
                        quadToRelative(-0.05f, 0.17f, -0.2f, 0.3f)
                        reflectiveQuadToRelative(-0.38f, 0.08f)
                        quadTo(12.25f, 21.33f, 10.6f, 19.39f)
                        reflectiveQuadTo(8.95f, 14.65f)
                        quadToRelative(0f, -1.25f, 0.9f, -2.1f)
                        reflectiveQuadTo(12.03f, 11.7f)
                        reflectiveQuadToRelative(2.18f, 0.85f)
                        reflectiveQuadToRelative(0.9f, 2.1f)
                        quadToRelative(0f, 0.82f, 0.63f, 1.39f)
                        reflectiveQuadTo(17.2f, 16.6f)
                        reflectiveQuadToRelative(1.45f, -0.56f)
                        reflectiveQuadToRelative(0.6f, -1.39f)
                        quadToRelative(0f, -2.9f, -2.13f, -4.88f)
                        reflectiveQuadTo(12.05f, 7.8f)
                        reflectiveQuadTo(6.98f, 9.77f)
                        reflectiveQuadTo(4.85f, 14.63f)
                        quadToRelative(0f, 0.6f, 0.11f, 1.5f)
                        reflectiveQuadToRelative(0.54f, 2.1f)
                        quadToRelative(0.08f, 0.22f, -0.01f, 0.4f)
                        reflectiveQuadTo(5.2f, 18.88f)
                        reflectiveQuadTo(4.81f, 18.86f)
                        quadTo(4.63f, 18.77f, 4.55f, 18.58f)
                        quadTo(4.18f, 17.6f, 4.01f, 16.64f)
                        quadTo(3.85f, 15.68f, 3.85f, 14.65f)
                        quadToRelative(0f, -3.33f, 2.41f, -5.58f)
                        reflectiveQuadTo(12.03f, 6.82f)
                        close()
                        moveToRelative(0f, -4.8f)
                        quadToRelative(1.6f, 0f, 3.13f, 0.39f)
                        reflectiveQuadTo(18.1f, 3.52f)
                        quadToRelative(0.22f, 0.13f, 0.26f, 0.3f)
                        reflectiveQuadTo(18.33f, 4.17f)
                        reflectiveQuadTo(18.08f, 4.45f)
                        reflectiveQuadTo(17.65f, 4.42f)
                        quadTo(16.33f, 3.75f, 14.91f, 3.39f)
                        reflectiveQuadTo(12.03f, 3.02f)
                        quadToRelative(-1.45f, 0f, -2.85f, 0.34f)
                        reflectiveQuadTo(6.5f, 4.42f)
                        quadTo(6.3f, 4.55f, 6.1f, 4.49f)
                        reflectiveQuadTo(5.8f, 4.22f)
                        reflectiveQuadTo(5.75f, 3.86f)
                        reflectiveQuadTo(6f, 3.57f)
                        quadTo(7.4f, 2.82f, 8.93f, 2.42f)
                        reflectiveQuadToRelative(3.1f, -0.4f)
                        close()
                        moveToRelative(0f, 7.23f)
                        quadToRelative(2.33f, 0f, 4f, 1.56f)
                        reflectiveQuadToRelative(1.67f, 3.84f)
                        quadToRelative(0f, 0.22f, -0.14f, 0.36f)
                        reflectiveQuadTo(17.2f, 15.15f)
                        quadToRelative(-0.2f, 0f, -0.35f, -0.14f)
                        quadTo(16.7f, 14.88f, 16.7f, 14.65f)
                        quadToRelative(0f, -1.88f, -1.39f, -3.14f)
                        reflectiveQuadTo(12.03f, 10.25f)
                        reflectiveQuadTo(8.76f, 11.51f)
                        reflectiveQuadTo(7.4f, 14.65f)
                        quadToRelative(0f, 2.03f, 0.7f, 3.44f)
                        reflectiveQuadToRelative(2.05f, 2.84f)
                        quadToRelative(0.15f, 0.15f, 0.15f, 0.35f)
                        reflectiveQuadToRelative(-0.15f, 0.35f)
                        reflectiveQuadTo(9.8f, 21.78f)
                        reflectiveQuadTo(9.45f, 21.63f)
                        quadTo(7.98f, 20.08f, 7.19f, 18.46f)
                        reflectiveQuadTo(6.4f, 14.65f)
                        quadToRelative(0f, -2.28f, 1.65f, -3.84f)
                        reflectiveQuadTo(12.03f, 9.25f)
                        close()
                        moveTo(12f, 14.15f)
                        quadToRelative(0.23f, 0f, 0.36f, 0.15f)
                        reflectiveQuadToRelative(0.14f, 0.35f)
                        quadToRelative(0f, 1.87f, 1.35f, 3.08f)
                        reflectiveQuadTo(17f, 18.93f)
                        quadToRelative(0.15f, 0f, 0.43f, -0.03f)
                        quadTo(17.7f, 18.88f, 18f, 18.83f)
                        quadToRelative(0.23f, -0.05f, 0.39f, 0.06f)
                        reflectiveQuadToRelative(0.21f, 0.34f)
                        quadToRelative(0.05f, 0.2f, -0.07f, 0.35f)
                        quadToRelative(-0.13f, 0.15f, -0.33f, 0.2f)
                        quadToRelative(-0.45f, 0.13f, -0.79f, 0.14f)
                        reflectiveQuadTo(17f, 19.93f)
                        quadToRelative(-2.22f, 0f, -3.86f, -1.5f)
                        reflectiveQuadTo(11.5f, 14.65f)
                        quadToRelative(0f, -0.2f, 0.14f, -0.35f)
                        reflectiveQuadTo(12f, 14.15f)
                        close()
                    }
                }
                .build()
        return _fingerprint!!
    }

private var _fingerprint: ImageVector? = null
