/*
 * SPDX-FileCopyrightText: 2024 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Stable
public class Paddings(
    public val extraSmall: Dp = 4.dp,
    public val small: Dp = 8.dp,
    public val medium: Dp = 12.dp,
    public val large: Dp = 16.dp,
    public val extraLarge: Dp = 24.dp,
)

private val LocalPaddings = staticCompositionLocalOf { Paddings() }

public val MaterialTheme.paddings: Paddings
    @Composable
    @ReadOnlyComposable
    get() = LocalPaddings.current
