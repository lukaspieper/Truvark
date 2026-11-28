/*
 * SPDX-FileCopyrightText: 2026 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.ui.views.browser

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import de.lukaspieper.truvark.R
import de.lukaspieper.truvark.ui.icons.MaterialSymbols

public enum class BrowserLayout(
    @StringRes public val label: Int,
    public val icon: ImageVector
) {
    LIST(
        label = R.string.browser_list_view,
        icon = MaterialSymbols.ViewList,
    ),
    GRID(
        label = R.string.browser_grid_view,
        icon = MaterialSymbols.ViewModule,
    )
}
