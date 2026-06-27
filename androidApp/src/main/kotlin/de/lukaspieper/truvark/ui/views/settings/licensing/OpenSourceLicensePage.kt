/*
 * SPDX-FileCopyrightText: 2022 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.ui.views.settings.licensing

import android.content.res.Resources
import androidx.annotation.RawRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import de.lukaspieper.truvark.R
import de.lukaspieper.truvark.ui.extensions.safeDrawingStart
import de.lukaspieper.truvark.ui.preview.PagePreviews
import de.lukaspieper.truvark.ui.preview.PreviewHost
import de.lukaspieper.truvark.ui.theme.paddings
import de.lukaspieper.truvark.ui.views.settings.licensing.License.GeneralPublicLicenseV30OrLater

@Composable
public fun OpenSourceLicensePage(
    navigateBack: () -> Unit,
    isExpandedLayout: Boolean,
    modifier: Modifier = Modifier
) {
    val resources = LocalResources.current
    val licensedItems by produceState(initialValue = emptyList()) {
        value = fetchLicenseItems(resources)
    }

    OpenSourceLicenseView(
        licensedItems = licensedItems,
        isExpandedLayout = isExpandedLayout,
        navigateBack = navigateBack,
        modifier = modifier
    )
}

private fun fetchLicenseItems(resources: Resources): List<LicensedItem> {
    val metadata = resources.readRawStringResource(R.raw.third_party_license_metadata)
    val licenseUris = resources.readRawStringResource(R.raw.third_party_licenses)

    val undetectedLicensedItems: List<LicensedItem> = listOf()

    val metadataRegex = Regex("^(\\d+):(\\d+)\\s(.+)\$")
    return metadata.lineSequence()
        .mapNotNull { line -> metadataRegex.matchEntire(line) }
        .map { matchResult ->
            val (uriStartIndex, uriLength, itemId) = matchResult.destructured
            val licenseUri = licenseUris.substring(uriStartIndex.toInt(), uriStartIndex.toInt() + uriLength.toInt())
            LicensedItem(itemId, License.getByUri(licenseUri))
        }
        .plus(undetectedLicensedItems)
        .distinctBy { it.id.lowercase() }
        .sortedBy { it.id.lowercase() }
        .toList()
}

private fun Resources.readRawStringResource(@RawRes id: Int): String {
    openRawResource(id).use { inputStream ->
        return inputStream.bufferedReader().readText()
    }
}

@Composable
public fun OpenSourceLicenseView(
    licensedItems: List<LicensedItem>,
    isExpandedLayout: Boolean,
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedLicense by remember { mutableStateOf<License>(License.UnknownLicense) }

    val containerColor = when {
        isExpandedLayout -> MaterialTheme.colorScheme.surfaceContainer
        else -> MaterialTheme.colorScheme.surface
    }
    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets.safeDrawing.exclude(WindowInsets.safeDrawingStart),
        containerColor = containerColor,
        topBar = {
            TopAppBar(
                windowInsets = TopAppBarDefaults.windowInsets.exclude(WindowInsets.safeDrawingStart),
                colors = TopAppBarDefaults.topAppBarColors().copy(containerColor = containerColor),
                title = { Text(text = stringResource(R.string.settings_licensing)) },
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
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.paddings.small),
            contentPadding = contentPadding + PaddingValues(all = MaterialTheme.paddings.large),
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                Card(
                    onClick = { selectedLicense = GeneralPublicLicenseV30OrLater },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(all = MaterialTheme.paddings.small)) {
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = GeneralPublicLicenseV30OrLater.name,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.size(MaterialTheme.paddings.extraLarge))
            }

            items(licensedItems) { licenseItem ->
                Card(
                    enabled = licenseItem.license != License.UnknownLicense,
                    onClick = { selectedLicense = licenseItem.license },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(all = MaterialTheme.paddings.small)) {
                        Text(
                            text = licenseItem.id,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = licenseItem.license.name,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }

    if (selectedLicense != License.UnknownLicense) {
        val resources = LocalResources.current
        val licenseText by produceState(initialValue = "") {
            selectedLicense.textResId?.let { textResId ->
                value = resources.readRawStringResource(textResId)
            }
        }

        Dialog(
            onDismissRequest = { selectedLicense = License.UnknownLicense },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                shape = MaterialTheme.shapes.extraLarge,
                modifier = Modifier
                    .padding(MaterialTheme.paddings.small)
                    .sizeIn(maxWidth = 600.dp)
            ) {
                Box(Modifier.padding(horizontal = MaterialTheme.paddings.small)) {
                    Column(Modifier.verticalScroll(rememberScrollState())) {
                        Text(
                            text = licenseText,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = MaterialTheme.paddings.medium, bottom = 72.dp)
                        )
                    }

                    FloatingActionButton(
                        onClick = { selectedLicense = License.UnknownLicense },
                        modifier = Modifier
                            .align(alignment = Alignment.BottomEnd)
                            .padding(bottom = MaterialTheme.paddings.small)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null)
                    }
                }
            }
        }
    }
}

@PagePreviews
@Composable
private fun OpenSourceLicenseViewPreview() = PreviewHost {
    val licensedItems = List(10) {
        LicensedItem("Item $it", License.UnknownLicense)
    }

    OpenSourceLicenseView(
        licensedItems = licensedItems,
        isExpandedLayout = false,
        navigateBack = {}
    )
}
