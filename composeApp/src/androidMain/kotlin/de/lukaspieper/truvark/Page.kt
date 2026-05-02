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
    public data object Browser : Page

    @Serializable
    public data class Presenter(val folderId: String, val fileId: String) : Page

    @Serializable
    public data object SettingsHome : Page
}
