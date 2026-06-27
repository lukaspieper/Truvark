/*
 * SPDX-FileCopyrightText: 2024 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.ui.preview

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import de.lukaspieper.truvark.ui.theme.AppTheme

@Composable
public fun PreviewHost(
    modifier: Modifier = Modifier.fillMaxSize(),
    backgroundColor: Color? = null,
    contentColor: Color? = null,
    content: @Composable () -> Unit
) {
    AppTheme {
        Surface(
            color = backgroundColor ?: MaterialTheme.colorScheme.surface,
            contentColor = contentColor ?: MaterialTheme.colorScheme.onSurface,
            modifier = modifier
        ) {
            content()
        }
    }
}
