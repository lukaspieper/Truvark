/*
 * SPDX-FileCopyrightText: 2024 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark

import androidx.compose.runtime.Immutable
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Immutable
public sealed interface Route : NavKey

@Immutable
public sealed interface SinglePaneRoute : Route {
    @Serializable
    public data object Launcher : SinglePaneRoute

    @Serializable
    public data class Browser(val vaultId: Uuid) : SinglePaneRoute

    @Serializable
    public data class Presenter(val vaultId: Uuid, val folderId: Uuid, val fileId: Uuid) : SinglePaneRoute
}

@Immutable
public sealed interface ListPaneRoute : Route {
    @Serializable
    public data class SettingsHome(val vaultId: Uuid?) : ListPaneRoute
}

@Immutable
public sealed interface DetailPaneRoute : Route {
    @Serializable
    public data class VaultSettings(val vaultId: Uuid) : DetailPaneRoute

    @Serializable
    public data object AppSettings : DetailPaneRoute

    @Serializable
    public data object Licenses : DetailPaneRoute
}
