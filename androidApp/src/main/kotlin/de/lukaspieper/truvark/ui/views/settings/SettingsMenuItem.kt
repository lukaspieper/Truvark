/*
 * SPDX-FileCopyrightText: 2024 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.ui.views.settings

import android.net.Uri
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector

public sealed interface SettingsMenuItem {
    public val icon: ImageVector
    public val title: Int
    public val description: Int?

    public enum class Key {
        VAULT,
        APP,
        OSS_LICENSES,
        SOURCE_CODE,
    }

    public data class Internal(
        override val icon: ImageVector,
        @StringRes override val title: Int,
        @StringRes override val description: Int? = null,
        val enabled: Boolean = true
    ) : SettingsMenuItem

    public data class External(
        override val icon: ImageVector,
        @StringRes override val title: Int,
        @StringRes override val description: Int? = null,
        val uri: Uri
    ) : SettingsMenuItem
}
