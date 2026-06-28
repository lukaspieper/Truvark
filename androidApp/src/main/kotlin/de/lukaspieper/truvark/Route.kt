/*
 * SPDX-FileCopyrightText: 2024 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

public sealed interface Route : NavKey {
    @Serializable
    public data object Launcher : Route

    @Serializable
    public data class Browser(val vaultId: String) : Route

    // Navigation does not support Uuid, using String here.
    @Serializable
    public data class Presenter(val vaultId: String, val folderId: String, val fileId: String) : Route
}

public sealed interface ListRoute : Route {

    @Serializable
    public data class SettingsHome(val vaultId: String?) : ListRoute
}

public sealed interface DetailRoute : Route {
    @Serializable
    public data class VaultSettings(val vaultId: String) : DetailRoute

    @Serializable
    public data object AppSettings : DetailRoute

    @Serializable
    public data object Licenses : DetailRoute
}
