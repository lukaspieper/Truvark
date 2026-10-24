/*
 * SPDX-FileCopyrightText: 2022 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import de.lukaspieper.truvark.data.preferences.PersistentPreferences
import de.lukaspieper.truvark.ui.theme.AppTheme
import de.lukaspieper.truvark.ui.views.browser.BrowserPage
import de.lukaspieper.truvark.ui.views.launcher.LauncherPage
import de.lukaspieper.truvark.ui.views.presenter.PresenterPage
import de.lukaspieper.truvark.ui.views.settings.SettingsHomePage
import de.lukaspieper.truvark.ui.views.settings.app.AppSettingsPage
import de.lukaspieper.truvark.ui.views.settings.licensing.OpenSourceLicensePage
import de.lukaspieper.truvark.ui.views.settings.vault.VaultSettingsPage
import kotlinx.coroutines.launch
import logcat.LogPriority
import logcat.logcat
import org.koin.android.scope.AndroidScopeComponent
import org.koin.androidx.scope.activityRetainedScope
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf
import org.koin.core.scope.Scope
import kotlin.uuid.Uuid

/**
 * This activity is the only one in this app.
 */
public class Activity : AppCompatActivity(), AndroidScopeComponent, KoinComponent {

    private val preferences: PersistentPreferences by inject()

    override val scope: Scope by activityRetainedScope()

    @OptIn(ExperimentalMaterial3AdaptiveApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        // Calling before onCreate because of this exception: https://stackoverflow.com/a/73129726
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        window.setHideOverlayWindows(true)
        collectScreenCapturePreference()

        setContent {
            AppTheme {
                val backStack = rememberNavBackStack(SinglePaneRoute.Launcher)

                LaunchedEffect(backStack) {
                    snapshotFlow { backStack.toList() }.collect { stack ->
                        logcat(LogPriority.DEBUG) {
                            "NavBackStack: ${stack.joinToString(" > ")}"
                        }
                    }
                }

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

                NavDisplay(
                    modifier = Modifier.background(MaterialTheme.colorScheme.background),
                    backStack = backStack,
                    onBack = backStack::removeLastOrNull,
                    sceneStrategies = listOf(listDetailStrategy),
                    entryDecorators = listOf(
                        rememberSaveableStateHolderNavEntryDecorator(),
                        rememberViewModelStoreNavEntryDecorator()
                    ),
                    transitionSpec = { EnterTransition.None togetherWith ExitTransition.None },
                    popTransitionSpec = { EnterTransition.None togetherWith ExitTransition.None },
                    predictivePopTransitionSpec = { EnterTransition.None togetherWith ExitTransition.None },
                    entryProvider = entryProvider {
                        entry<SinglePaneRoute.Launcher> {
                            LauncherPage(
                                navigateTo = { route -> backStack.goToRoute(route, isExpandedLayout) },
                                viewModel = koinViewModel()
                            )
                        }

                        entry<SinglePaneRoute.Browser> { route ->
                            BrowserPage(
                                route = route,
                                navigateTo = { route -> backStack.goToRoute(route, isExpandedLayout) },
                                navigateBack = backStack::removeLastOrNull,
                                viewModel = koinViewModel(scope = getScope(route.vaultId))
                            )
                        }

                        entry<SinglePaneRoute.Presenter> { route ->
                            PresenterPage(
                                route = route,
                                navigateBack = backStack::removeLastOrNull,
                                viewModel = koinViewModel(scope = getScope(route.vaultId)) {
                                    parametersOf(route.folderId)
                                }
                            )
                        }

                        entry<ListPaneRoute.SettingsHome>(metadata = ListDetailSceneStrategy.listPane()) { route ->
                            SettingsHomePage(
                                route = route,
                                // Returning only in expanded layout avoids highlighting the menu item for a moment.
                                currentDetailPaneRoute = when {
                                    isExpandedLayout -> backStack.lastOrNull() as? DetailPaneRoute
                                    else -> null
                                },
                                navigateBack = { backStack.goBackTo<SinglePaneRoute>() },
                                navigateToDetailPane = { route -> backStack.goToRoute(route, isExpandedLayout) },
                                isExpandedLayout = isExpandedLayout
                            )
                        }

                        entry<DetailPaneRoute.VaultSettings>(metadata = ListDetailSceneStrategy.detailPane()) { route ->
                            VaultSettingsPage(
                                navigateBack = backStack::removeLastOrNull,
                                viewModel = koinViewModel(scope = getScope(route.vaultId)),
                                isExpandedLayout = isExpandedLayout
                            )
                        }

                        entry<DetailPaneRoute.AppSettings>(metadata = ListDetailSceneStrategy.detailPane()) {
                            AppSettingsPage(
                                navigateBack = backStack::removeLastOrNull,
                                isExpandedLayout = isExpandedLayout,
                                viewModel = koinViewModel()
                            )
                        }

                        entry<DetailPaneRoute.Licenses>(metadata = ListDetailSceneStrategy.detailPane()) {
                            OpenSourceLicensePage(
                                navigateBack = backStack::removeLastOrNull,
                                isExpandedLayout = isExpandedLayout,
                                viewModel = koinViewModel()
                            )
                        }
                    }
                )
            }
        }
    }

    private fun collectScreenCapturePreference() {
        fun allowScreenCapture(allow: Boolean) {
            when {
                allow -> window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                else -> window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
            }
        }

        // Disable by default before first value becomes available to prevent any leaks.
        allowScreenCapture(false)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                preferences.allowScreenCapture.collect(::allowScreenCapture)
            }
        }
    }

    private fun KoinComponent.getScope(scopeId: Uuid): Scope {
        return getKoin().getScope(scopeId.toHexString())
    }

    /**
     * Pushes the given route onto the back stack, skipping it if the top entry has the same route class. This guards
     * against double taps opening the same screen twice (e.g. a presenter). Only the top entry is checked. Detail
     * routes are exempt: they replace the current detail pane by popping back to the list pane first, so the last tap
     * wins. In expanded layout, a list pane route also pushes its default detail route.
     *
     * Maybe consider using `dropUnlessResumed` instead for some cases?
     */
    private fun NavBackStack<NavKey>.goToRoute(route: Route, isExpandedLayout: Boolean) {
        if (route is DetailPaneRoute) {
            goBackTo<ListPaneRoute>()
        }

        val current = lastOrNull()

        if (current == null || current::class != route::class) {
            add(route)

            if (route is ListPaneRoute && isExpandedLayout) {
                add(route.defaultDetailPaneRoute)
            }
        }
    }

    private inline fun <reified T : Route> NavBackStack<NavKey>.goBackTo() {
        val index = indexOfLast { it is T }
        if (index == -1) return

        while (lastIndex > index) {
            removeAt(lastIndex)
        }
    }
}
