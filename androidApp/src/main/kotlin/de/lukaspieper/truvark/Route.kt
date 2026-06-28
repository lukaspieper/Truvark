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
public sealed interface Route : NavKey {
    @Serializable
    public data object Launcher : Route

    @Serializable
    public data class Browser(val vaultId: Uuid) : Route

    @Serializable
    public data class Presenter(val vaultId: Uuid, val folderId: Uuid, val fileId: Uuid) : Route
}

@Immutable
public sealed interface ListRoute : Route {

    @Serializable
    public data class SettingsHome(val vaultId: Uuid?) : ListRoute
}

@Immutable
public sealed interface DetailRoute : Route {
    @Serializable
    public data class VaultSettings(val vaultId: Uuid) : DetailRoute

    @Serializable
    public data object AppSettings : DetailRoute

    @Serializable
    public data object Licenses : DetailRoute
}
