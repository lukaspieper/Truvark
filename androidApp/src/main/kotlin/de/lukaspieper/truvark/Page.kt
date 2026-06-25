/*
 * SPDX-FileCopyrightText: 2024 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark

import androidx.compose.runtime.mutableStateListOf
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

public class Navigator(startDestination: Page) {
    private val _backStack = mutableStateListOf(startDestination)

    public val backStack: List<Page>
        get() = _backStack

    public fun goTo(destination: Page) {
        _backStack.add(destination)
    }

    public fun goToReplace(destination: Page) {
        _backStack[_backStack.lastIndex] = destination
    }

    public fun goBack() {
        _backStack.removeLastOrNull()
    }
}

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
    public data class SettingsHome(val vaultId: String) : Page
}
