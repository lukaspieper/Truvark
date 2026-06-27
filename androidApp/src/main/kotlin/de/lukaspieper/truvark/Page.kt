/*
 * SPDX-FileCopyrightText: 2024 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

public sealed interface DetailPage : Page

@Serializable
public sealed interface Page : NavKey {
    @Serializable
    public data object Launcher : Page

    @Serializable
    public data class Browser(val vaultId: String) : Page

    // Navigation does not support Uuid, using String here.
    @Serializable
    public data class Presenter(val vaultId: String, val folderId: String, val fileId: String) : Page

    @Serializable
    public data class SettingsHome(val vaultId: String?) : Page

    @Serializable
    public data class VaultSettings(val vaultId: String) : DetailPage

    @Serializable
    public data object AppSettings : DetailPage

    @Serializable
    public data object Licenses : DetailPage
}
