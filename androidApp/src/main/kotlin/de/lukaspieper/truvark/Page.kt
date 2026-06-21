/*
 * SPDX-FileCopyrightText: 2024 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark

import kotlinx.serialization.Serializable

@Serializable
public sealed interface Page {
    @Serializable
    public data object Launcher : Page

    @Serializable
    public data class Browser(val vaultId: String) : Page

    // Navigation does not support Uuid, using String here.
    @Serializable
    public data class Presenter(val vaultId: String, val folderId: String, val fileId: String) : Page

    @Serializable
    public data class SettingsHome(val vaultId: String) : Page
}
