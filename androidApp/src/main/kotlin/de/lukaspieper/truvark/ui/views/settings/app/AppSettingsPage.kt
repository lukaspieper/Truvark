/*
 * SPDX-FileCopyrightText: 2024 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.ui.views.settings.app

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import de.lukaspieper.truvark.ui.controls.LabeledSwitch
import de.lukaspieper.truvark.ui.extensions.exclude
import de.lukaspieper.truvark.ui.preview.PagePreviews
import de.lukaspieper.truvark.ui.preview.PreviewHost
import de.lukaspieper.truvark.ui.theme.paddings

@Composable
public fun AppSettingsPage(
    navigateBack: () -> Unit,
    isExpandedLayout: Boolean,
    viewModel: AppSettingsViewModel,
    modifier: Modifier = Modifier
) {
    val imagesFitScreen = viewModel.imagesFitScreen.collectAsStateWithLifecycle(false)
    val isLoggingEnabled = viewModel.isLoggingEnabled.collectAsStateWithLifecycle(false)

    AppSettingsView(
        imagesFitScreen = imagesFitScreen.value,
        updateImagesFitScreen = viewModel::applyImagesFitScreen,
        isLoggingEnabled = isLoggingEnabled.value,
        updateIsLoggingEnabled = viewModel::applyLogging,
        isExpandedLayout = isExpandedLayout,
        navigateBack = navigateBack,
        modifier = modifier
    )
}

@Composable
private fun AppSettingsView(
    imagesFitScreen: Boolean,
    updateImagesFitScreen: (Boolean) -> Unit,
    isLoggingEnabled: Boolean,
    updateIsLoggingEnabled: (Boolean) -> Unit,
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
        Column(modifier = Modifier.padding(contentPadding + PaddingValues(all = MaterialTheme.paddings.large))) {
            LabeledSwitch(
                text = stringResource(R.string.fit_images_to_match_screen),
                checked = imagesFitScreen,
                onCheckedChange = updateImagesFitScreen,
                modifier = Modifier.padding(horizontal = MaterialTheme.paddings.small)
            )

            LabeledSwitch(
                text = stringResource(R.string.logging),
                checked = isLoggingEnabled,
                onCheckedChange = updateIsLoggingEnabled,
                modifier = Modifier.padding(horizontal = MaterialTheme.paddings.small)
            )
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
        isExpandedLayout = false,
        navigateBack = {}
    )
}
