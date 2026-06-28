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
import de.lukaspieper.truvark.DetailRoute
import de.lukaspieper.truvark.ListRoute
import de.lukaspieper.truvark.R
import de.lukaspieper.truvark.Route
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
    route: ListRoute.SettingsHome,
    currentDetailRoute: DetailRoute?,
    navigateBack: () -> Unit,
    navigateTo: (Route) -> Unit,
    isExpandedLayout: Boolean,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(navigateTo, isExpandedLayout) {
        // Do initial navigation when both panes are visible.
        if (isExpandedLayout && currentDetailRoute == null) {
            navigateTo(
                when {
                    route.vaultId != null -> DetailRoute.VaultSettings(vaultId = route.vaultId)
                    else -> DetailRoute.AppSettings
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
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.paddings.small),
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(contentPadding + PaddingValues(all = MaterialTheme.paddings.large))
            ) {
                val isVaultSettingsSelected = currentDetailRoute is DetailRoute.VaultSettings
                SegmentedListItem(
                    onClick = { navigateTo(DetailRoute.VaultSettings(vaultId = route.vaultId!!)) },
                    shapes = roundShape,
                    colors = if (isVaultSettingsSelected) selectedColors else defaultColors,
                    enabled = !isVaultSettingsSelected && route.vaultId != null,
                    leadingContent = { Icon(Icons.Default.Lock, contentDescription = null) },
                    content = { Text(stringResource(R.string.vault)) },
                    supportingContent = { Text(stringResource(R.string.settings_description_vault)) },
                )

                val isAppSettingsSelected = currentDetailRoute is DetailRoute.AppSettings
                SegmentedListItem(
                    onClick = { navigateTo(DetailRoute.AppSettings) },
                    shapes = roundShape,
                    colors = if (isAppSettingsSelected) selectedColors else defaultColors,
                    enabled = !isAppSettingsSelected,
                    leadingContent = { Icon(Icons.Default.AppSettingsAlt, contentDescription = null) },
                    content = { Text(stringResource(R.string.app)) },
                    supportingContent = { Text(stringResource(R.string.settings_description_app)) },
                )

                val isLicensesSelected = currentDetailRoute is DetailRoute.Licenses
                SegmentedListItem(
                    onClick = { navigateTo(DetailRoute.Licenses) },
                    shapes = roundShape,
                    colors = if (isLicensesSelected) selectedColors else defaultColors,
                    enabled = !isLicensesSelected,
                    leadingContent = { Icon(Icons.Default.Copyright, contentDescription = null) },
                    content = { Text(stringResource(R.string.settings_licensing)) },
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
                    shapes = roundShape,
                    colors = defaultColors,
                    leadingContent = { Icon(Icons.Default.Code, contentDescription = null) },
                    content = { Text(stringResource(R.string.source_code)) },
                    trailingContent = { Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null) },
                )
            }
        }
    )
}

@PagePreviews
@Composable
private fun SettingsViewPreview() = PreviewHost {
    SettingsHomePage(
        route = ListRoute.SettingsHome(vaultId = null),
        navigateBack = {},
        navigateTo = {},
        isExpandedLayout = true,
        currentDetailRoute = DetailRoute.Licenses,
    )
}
