/*
 * SPDX-FileCopyrightText: 2022 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.ui.views.settings.licensing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import de.lukaspieper.truvark.R
import de.lukaspieper.truvark.ui.controls.SingleLineText
import de.lukaspieper.truvark.ui.extensions.exclude
import de.lukaspieper.truvark.ui.preview.PagePreviews
import de.lukaspieper.truvark.ui.preview.PreviewHost
import de.lukaspieper.truvark.ui.theme.paddings
import de.lukaspieper.truvark.ui.views.settings.licensing.License.GeneralPublicLicenseV30OrLater

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
public fun OpenSourceLicensePage(
    navigateBack: () -> Unit,
    isExpandedLayout: Boolean,
    viewModel: OpenSourceLicenseViewModel,
    modifier: Modifier = Modifier
) {
    OpenSourceLicenseView(
        licensedItems = viewModel.licensedItems,
        selectedLicense = viewModel.selectedLicense,
        selectedLicenseText = viewModel.selectedLicenseText,
        selectLicense = viewModel::selectLicense,
        dismissRequest = viewModel::clearSelectedLicense,
        isExpandedLayout = isExpandedLayout,
        navigateBack = navigateBack,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
public fun OpenSourceLicenseView(
    licensedItems: List<LicensedItem>,
    selectedLicense: License,
    selectedLicenseText: String,
    selectLicense: (License) -> Unit,
    dismissRequest: () -> Unit,
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
                title = { Text(text = stringResource(R.string.settings_legal)) },
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
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
            contentPadding = contentPadding + PaddingValues(all = MaterialTheme.paddings.large),
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                SegmentedListItem(
                    onClick = { selectLicense(GeneralPublicLicenseV30OrLater) },
                    colors = ListItemDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        leadingContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        supportingContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    shapes = ListItemDefaults.shapes(MaterialTheme.shapes.extraLarge),
                    leadingContent = {
                        Icon(
                            painterResource(R.drawable.ic_truvark),
                            contentDescription = null,
                            modifier = Modifier.size(32.dp)
                        )
                    },
                    content = { Text(text = stringResource(R.string.app_name)) },
                    supportingContent = { Text(text = GeneralPublicLicenseV30OrLater.name) }
                )

                Spacer(modifier = Modifier.size(MaterialTheme.paddings.extraLarge))
            }

            itemsIndexed(licensedItems) { index, licenseItem ->
                SegmentedListItem(
                    enabled = licenseItem.license != License.UnknownLicense,
                    onClick = { selectLicense(licenseItem.license) },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                    shapes = ListItemDefaults.segmentedShapes(index = index, count = licensedItems.size),
                    content = { Text(text = licenseItem.id) },
                    supportingContent = { Text(text = licenseItem.license.name) }
                )
            }
        }
    }

    if (selectedLicense != License.UnknownLicense) {
        LicenseTextDialog(
            license = selectedLicense,
            licenseText = selectedLicenseText,
            dismissRequest = dismissRequest
        )
    }
}

@Composable
private fun LicenseTextDialog(
    license: License,
    licenseText: String,
    dismissRequest: () -> Unit
) {
    Dialog(
        onDismissRequest = { dismissRequest() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = MaterialTheme.shapes.extraLarge,
            modifier = Modifier
                .padding(MaterialTheme.paddings.small)
                .sizeIn(maxWidth = 600.dp)
        ) {
            Column {
                TopAppBar(
                    title = {
                        SingleLineText(text = license.name)
                    },
                    actions = {
                        IconButton(onClick = dismissRequest) {
                            Icon(Icons.Default.Close, contentDescription = null)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        actionIconContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )

                Box(Modifier.padding(horizontal = MaterialTheme.paddings.medium)) {
                    Column(Modifier.verticalScroll(rememberScrollState())) {
                        Text(
                            text = licenseText,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = MaterialTheme.paddings.medium)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@PagePreviews
@Composable
private fun OpenSourceLicenseViewPreview() = PreviewHost {
    OpenSourceLicenseView(
        licensedItems = List(16) { LicensedItem("Item $it", License.ApacheLicenseV20) },
        selectedLicense = License.UnknownLicense,
        selectedLicenseText = "",
        selectLicense = {},
        dismissRequest = {},
        isExpandedLayout = false,
        navigateBack = {}
    )
}

@PagePreviews
@Composable
private fun LicenseTextDialogPreview() = PreviewHost {
    LicenseTextDialog(
        license = License.ApacheLicenseV20,
        licenseText = """
            Apache License
            Version 2.0, January 2004
            http://www.apache.org/licenses/

            TERMS AND CONDITIONS FOR USE, REPRODUCTION, AND DISTRIBUTION

            1. Definitions.
            
            [...]
        """.trimIndent(),
        dismissRequest = {}
    )
}
