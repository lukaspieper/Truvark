/*
 * SPDX-FileCopyrightText: 2022 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.ui.views.settings

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AppSettingsAlt
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Copyright
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import de.lukaspieper.truvark.DetailPaneRoute
import de.lukaspieper.truvark.ListPaneRoute
import de.lukaspieper.truvark.R
import de.lukaspieper.truvark.ui.controls.SingleLineText
import de.lukaspieper.truvark.ui.extensions.exclude
import de.lukaspieper.truvark.ui.preview.PagePreviews
import de.lukaspieper.truvark.ui.preview.PreviewHost
import de.lukaspieper.truvark.ui.theme.paddings
import logcat.LogPriority
import logcat.asLog
import logcat.logcat

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
public fun SettingsHomePage(
    route: ListPaneRoute.SettingsHome,
    currentDetailPaneRoute: DetailPaneRoute?,
    navigateBack: () -> Unit,
    navigateToDetailPane: (DetailPaneRoute) -> Unit,
    isExpandedLayout: Boolean,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(navigateToDetailPane, isExpandedLayout) {
        // Do initial navigation when both panes are visible.
        if (isExpandedLayout && currentDetailPaneRoute == null) {
            navigateToDetailPane(
                when {
                    route.vaultId != null -> DetailPaneRoute.VaultSettings(vaultId = route.vaultId)
                    else -> DetailPaneRoute.AppSettings
                }
            )
        }
    }

    val context = LocalContext.current
    Scaffold(
        modifier = modifier,
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.exclude(WindowInsetsSides.End),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = {
                    IconButton(
                        onClick = navigateBack,
                        content = { Icon(Icons.AutoMirrored.Default.ArrowBack, null) }
                    )
                }
            )
        },
        content = { contentPadding ->
            val roundShape = ListItemDefaults.shapes(MaterialTheme.shapes.large)
            val defaultColors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            val selectedColors = ListItemDefaults.colors(
                disabledContainerColor = MaterialTheme.colorScheme.primaryContainer,
                disabledContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                disabledLeadingContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                disabledSupportingContentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.paddings.large),
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(contentPadding + PaddingValues(all = MaterialTheme.paddings.large))
            ) {
                val isVaultSettingsSelected = currentDetailPaneRoute is DetailPaneRoute.VaultSettings
                SegmentedListItem(
                    onClick = { navigateToDetailPane(DetailPaneRoute.VaultSettings(vaultId = route.vaultId!!)) },
                    shapes = roundShape,
                    colors = if (isVaultSettingsSelected) selectedColors else defaultColors,
                    enabled = !isVaultSettingsSelected && route.vaultId != null,
                    leadingContent = { Icon(Icons.Default.Lock, contentDescription = null) },
                    content = { Text(stringResource(R.string.vault)) },
                    supportingContent = { SingleLineText(stringResource(R.string.settings_description_vault)) },
                )

                val isAppSettingsSelected = currentDetailPaneRoute is DetailPaneRoute.AppSettings
                SegmentedListItem(
                    onClick = { navigateToDetailPane(DetailPaneRoute.AppSettings) },
                    shapes = roundShape,
                    colors = if (isAppSettingsSelected) selectedColors else defaultColors,
                    enabled = !isAppSettingsSelected,
                    leadingContent = { Icon(Icons.Default.AppSettingsAlt, contentDescription = null) },
                    content = { Text(stringResource(R.string.app)) },
                    supportingContent = { SingleLineText(stringResource(R.string.settings_description_app)) },
                )

                Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
                    val isLicensesSelected = currentDetailPaneRoute is DetailPaneRoute.Licenses
                    SegmentedListItem(
                        onClick = { navigateToDetailPane(DetailPaneRoute.Licenses) },
                        shapes = ListItemDefaults.segmentedShapes(index = 0, count = 2),
                        colors = if (isLicensesSelected) selectedColors else defaultColors,
                        enabled = !isLicensesSelected,
                        leadingContent = { Icon(Icons.Default.Copyright, contentDescription = null) },
                        content = { Text(stringResource(R.string.settings_legal)) },
                        supportingContent = { SingleLineText(stringResource(R.string.settings_legal_descriptioin)) }
                    )

                    SegmentedListItem(
                        onClick = {
                            try {
                                val browserIntent =
                                    Intent(Intent.ACTION_VIEW, "https://github.com/lukaspieper/Truvark".toUri())
                                context.startActivity(browserIntent)
                            } catch (e: Exception) {
                                logcat("SettingsHomePage", LogPriority.WARN) { e.asLog() }
                            }
                        },
                        shapes = ListItemDefaults.segmentedShapes(index = 1, count = 2),
                        colors = defaultColors,
                        leadingContent = { Icon(Icons.Default.Code, contentDescription = null) },
                        content = { Text(stringResource(R.string.settings_repository)) },
                        supportingContent = {
                            SingleLineText(stringResource(R.string.settings_repository_description))
                        },
                        trailingContent = { Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null) },
                    )
                }
            }
        }
    )
}

@PagePreviews
@Composable
private fun SettingsViewPreview() = PreviewHost {
    SettingsHomePage(
        route = ListPaneRoute.SettingsHome(vaultId = null),
        navigateBack = {},
        navigateToDetailPane = {},
        isExpandedLayout = false,
        currentDetailPaneRoute = null
    )
}
