/*
 * SPDX-FileCopyrightText: 2026 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.ui.views.settings

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import de.lukaspieper.truvark.ui.controls.SettingsSectionText
import de.lukaspieper.truvark.ui.theme.paddings

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
public fun SettingsSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(
        verticalArrangement = spacedBy(MaterialTheme.paddings.small),
        modifier = modifier
    ) {
        SettingsSectionText(text = title)

        Column(verticalArrangement = spacedBy(ListItemDefaults.SegmentedGap)) {
            content()
        }
    }
}

@Composable
public fun CardSettingsSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(
        verticalArrangement = spacedBy(MaterialTheme.paddings.small),
        modifier = modifier
    ) {
        SettingsSectionText(text = title)

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
            shape = MaterialTheme.shapes.large,
        ) {
            Column(modifier = Modifier.padding(MaterialTheme.paddings.large)) {
                content()
            }
        }
    }
}
