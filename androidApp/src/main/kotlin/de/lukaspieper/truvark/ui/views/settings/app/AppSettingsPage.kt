/*
 * SPDX-FileCopyrightText: 2024 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.ui.views.settings.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.lukaspieper.truvark.R
import de.lukaspieper.truvark.ui.controls.SegmentedSwitchListItem
import de.lukaspieper.truvark.ui.extensions.exclude
import de.lukaspieper.truvark.ui.preview.PagePreviews
import de.lukaspieper.truvark.ui.preview.PreviewHost
import de.lukaspieper.truvark.ui.theme.paddings
import de.lukaspieper.truvark.ui.views.settings.SettingsSection

@Composable
public fun AppSettingsPage(
    navigateBack: () -> Unit,
    isExpandedLayout: Boolean,
    viewModel: AppSettingsViewModel,
    modifier: Modifier = Modifier
) {
    val imagesFitScreen = viewModel.imagesFitScreen.collectAsStateWithLifecycle(null)
    val isLoggingEnabled = viewModel.isLoggingEnabled.collectAsStateWithLifecycle(null)
    val allowScreenCapture = viewModel.allowScreenCapture.collectAsStateWithLifecycle(null)

    AppSettingsView(
        imagesFitScreen = imagesFitScreen.value,
        updateImagesFitScreen = viewModel::applyImagesFitScreen,
        isLoggingEnabled = isLoggingEnabled.value,
        updateIsLoggingEnabled = viewModel::applyLogging,
        allowScreenCapture = allowScreenCapture.value,
        updateAllowScreenCapture = viewModel::applyAllowScreenCapture,
        isExpandedLayout = isExpandedLayout,
        navigateBack = navigateBack,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AppSettingsView(
    imagesFitScreen: Boolean?,
    updateImagesFitScreen: (Boolean) -> Unit,
    isLoggingEnabled: Boolean?,
    updateIsLoggingEnabled: (Boolean) -> Unit,
    allowScreenCapture: Boolean?,
    updateAllowScreenCapture: (Boolean) -> Unit,
    isExpandedLayout: Boolean,
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = when {
        isExpandedLayout -> MaterialTheme.colorScheme.surfaceContainer
        else -> MaterialTheme.colorScheme.surface
    }
    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets.safeDrawing.exclude(WindowInsetsSides.Start),
        containerColor = containerColor,
        topBar = {
            TopAppBar(
                windowInsets = TopAppBarDefaults.windowInsets.exclude(WindowInsetsSides.Start),
                colors = TopAppBarDefaults.topAppBarColors().copy(containerColor = containerColor),
                title = { Text(text = stringResource(R.string.app)) },
                navigationIcon = {
                    if (!isExpandedLayout) {
                        IconButton(onClick = navigateBack) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, null)
                        }
                    }
                }
            )
        }
    ) { contentPadding ->
        Column(
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.paddings.large),
            modifier = Modifier.padding(contentPadding + PaddingValues(all = MaterialTheme.paddings.large))
        ) {
            SettingsSection(stringResource(R.string.appearance)) {
                SegmentedSwitchListItem(
                    text = stringResource(R.string.fit_images_to_match_screen),
                    checked = imagesFitScreen,
                    onCheckedChange = updateImagesFitScreen,
                )
            }

            SettingsSection(stringResource(R.string.debugging)) {
                SegmentedSwitchListItem(
                    text = stringResource(R.string.logging),
                    supportingText = stringResource(R.string.logging_description),
                    checked = isLoggingEnabled,
                    onCheckedChange = updateIsLoggingEnabled,
                    shapes = ListItemDefaults.segmentedShapes(index = 0, count = 2),
                )
                SegmentedSwitchListItem(
                    text = stringResource(R.string.allow_screen_capture),
                    supportingText = stringResource(R.string.allow_screen_capture_description),
                    checked = allowScreenCapture,
                    onCheckedChange = updateAllowScreenCapture,
                    shapes = ListItemDefaults.segmentedShapes(index = 1, count = 2),
                )
            }
        }
    }
}

@PagePreviews
@Composable
private fun AppSettingsViewPreview() = PreviewHost {
    AppSettingsView(
        imagesFitScreen = false,
        updateImagesFitScreen = {},
        isLoggingEnabled = false,
        updateIsLoggingEnabled = {},
        allowScreenCapture = false,
        updateAllowScreenCapture = {},
        isExpandedLayout = false,
        navigateBack = {}
    )
}
