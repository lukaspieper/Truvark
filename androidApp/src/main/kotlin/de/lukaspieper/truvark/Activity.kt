/*
 * SPDX-FileCopyrightText: 2022 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import de.lukaspieper.truvark.ui.theme.AppTheme
import de.lukaspieper.truvark.ui.views.browser.BrowserPage
import de.lukaspieper.truvark.ui.views.launcher.LauncherPage
import de.lukaspieper.truvark.ui.views.presenter.PresenterPage
import de.lukaspieper.truvark.ui.views.settings.SettingsHomePage
import de.lukaspieper.truvark.ui.views.settings.app.AppSettingsPage
import de.lukaspieper.truvark.ui.views.settings.licensing.OpenSourceLicensePage
import de.lukaspieper.truvark.ui.views.settings.vault.VaultSettingsPage
import org.koin.android.scope.AndroidScopeComponent
import org.koin.androidx.scope.activityRetainedScope
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinDelicateAPI
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.component.KoinComponent
import org.koin.core.parameter.parametersOf
import org.koin.core.scope.Scope
import kotlin.uuid.Uuid

/**
 * This activity is the only one in this app.
 */
public class Activity : AppCompatActivity(), AndroidScopeComponent, KoinComponent {

    override val scope: Scope by activityRetainedScope()

    @OptIn(KoinDelicateAPI::class, KoinExperimentalAPI::class, ExperimentalMaterial3AdaptiveApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        // Calling before onCreate because of this exception: https://stackoverflow.com/a/73129726
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                val backStack = rememberNavBackStack(Page.Launcher)

                // Override the defaults so that there isn't a horizontal space between the panes.
                // See b/418201867
                val windowAdaptiveInfo = currentWindowAdaptiveInfoV2()
                val directive = remember(windowAdaptiveInfo) {
                    calculatePaneScaffoldDirective(windowAdaptiveInfo).copy(horizontalPartitionSpacerSize = 0.dp)
                }
                val listDetailStrategy = rememberListDetailSceneStrategy<NavKey>(directive = directive)
                val isExpandedLayout by remember(listDetailStrategy) {
                    derivedStateOf { listDetailStrategy.directive.maxHorizontalPartitions > 1 }
                }

                // TODO: Handle multi-touch navigation conflicts
                NavDisplay(
                    modifier = Modifier.background(MaterialTheme.colorScheme.background),
                    backStack = backStack,
                    onBack = backStack::removeLastOrNull,
                    sceneStrategies = listOf(listDetailStrategy),
                    entryDecorators = listOf(
                        rememberSaveableStateHolderNavEntryDecorator(),
                        rememberViewModelStoreNavEntryDecorator()
                    ),
                    entryProvider = entryProvider {
                        entry<Page.Launcher> {
                            LauncherPage(
                                navigateAndClearBackStack = { route -> backStack.goToReplace(route) },
                                navigateTo = { route -> backStack.add(route) },
                                viewModel = koinViewModel()
                            )
                        }

                        entry<Page.SettingsHome>(metadata = ListDetailSceneStrategy.listPane()) { route ->
                            SettingsHomePage(
                                navigateBack = { backStack.goBackTo(Page.Launcher, Page.Browser(route.vaultId ?: "")) },
                                navigateTo = { page -> backStack.goToSettingsSubPage(page) },
                                vaultId = route.vaultId,
                                isExpandedLayout = isExpandedLayout,
                                currentPage = backStack.lastOrNull()
                            )
                        }

                        entry<Page.AppSettings>(metadata = ListDetailSceneStrategy.detailPane()) {
                            AppSettingsPage(
                                navigateBack = backStack::removeLastOrNull,
                                isExpandedLayout = isExpandedLayout,
                                viewModel = koinViewModel()
                            )
                        }

                        entry<Page.Licenses>(metadata = ListDetailSceneStrategy.detailPane()) {
                            OpenSourceLicensePage(
                                navigateBack = backStack::removeLastOrNull,
                                isExpandedLayout = isExpandedLayout
                            )
                        }

                        entry<Page.Browser> { route ->
                            BrowserPage(
                                parameters = route,
                                navigate = { route -> backStack.add(route) },
                                viewModel = koinViewModel(scope = getKoin().getScope(route.vaultId))
                            )
                        }

                        entry<Page.Presenter> { route ->
                            PresenterPage(
                                parameters = route,
                                navigateBack = backStack::removeLastOrNull,
                                viewModel = koinViewModel(scope = getKoin().getScope(route.vaultId)) {
                                    parametersOf(Uuid.parseHex(route.folderId))
                                }
                            )
                        }

                        entry<Page.VaultSettings>(metadata = ListDetailSceneStrategy.detailPane()) { route ->
                            VaultSettingsPage(
                                navigateBack = backStack::removeLastOrNull,
                                viewModel = koinViewModel(scope = getKoin().getScope(route.vaultId)),
                                isExpandedLayout = isExpandedLayout
                            )
                        }
                    }
                )
            }
        }
    }
}

public fun NavBackStack<NavKey>.goToReplace(destination: Page) {
    set(lastIndex, destination)
}

public fun NavBackStack<NavKey>.goToSettingsSubPage(destination: Page) {
    // Remove all pages from the back stack until we reach the settings home page.
    while (lastOrNull() !is Page.SettingsHome) {
        removeLastOrNull()
    }
    add(destination)
}

public fun NavBackStack<NavKey>.goBackTo(vararg destinations: Page) {
    val destination = destinations.lastOrNull { it in this } ?: return
    while (lastOrNull() != destination) {
        removeLastOrNull()
    }
}
