/*
 * SPDX-FileCopyrightText: 2024 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.ui.extensions

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.runtime.Composable

public val WindowInsets.Companion.safeDrawingTopAppBar: WindowInsets
    @Composable
    get() = WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)

public val WindowInsets.Companion.safeDrawingStart: WindowInsets
    @Composable
    get() = WindowInsets.safeDrawing.only(WindowInsetsSides.Start)

public val WindowInsets.Companion.safeDrawingStartTop: WindowInsets
    @Composable
    get() = WindowInsets.safeDrawing.only(WindowInsetsSides.Start + WindowInsetsSides.Top)

public val WindowInsets.Companion.safeDrawingEnd: WindowInsets
    @Composable
    get() = WindowInsets.safeDrawing.only(WindowInsetsSides.End)

public val WindowInsets.Companion.safeDrawingEndTop: WindowInsets
    @Composable
    get() = WindowInsets.safeDrawing.only(WindowInsetsSides.End + WindowInsetsSides.Top)
