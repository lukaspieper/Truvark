/*
 * SPDX-FileCopyrightText: 2026 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.ui.views.launcher

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import de.lukaspieper.truvark.R
import de.lukaspieper.truvark.ui.controls.InfoCard
import de.lukaspieper.truvark.ui.controls.InfoCardState
import de.lukaspieper.truvark.ui.controls.PageIndicator
import de.lukaspieper.truvark.ui.preview.BooleanPreviewParameterProvider
import de.lukaspieper.truvark.ui.preview.PreviewHost
import de.lukaspieper.truvark.ui.theme.paddings

@Composable
public fun LauncherInfoCardPager(
    isAnyDebuggingSettingEnabled: Boolean,
    isVaultAvailable: Boolean,
    modifier: Modifier = Modifier
) {
    val infoCardStates = rememberLauncherInfoCardStates(
        isAnyDebuggingSettingEnabled = isAnyDebuggingSettingEnabled,
        isVaultAvailable = isVaultAvailable
    )

    Column(modifier = modifier) {
        val pagerState = rememberPagerState(pageCount = { infoCardStates.size })
        HorizontalPager(
            state = pagerState,
            verticalAlignment = Alignment.Top,
            pageSpacing = MaterialTheme.paddings.small
        ) { page ->
            val infoCardState = infoCardStates[page]
            InfoCard(infoCardState, Modifier.fillMaxWidth())
        }

        PageIndicator(
            pagerState = pagerState,
            itemSize = infoCardStates.size,
            modifier = Modifier.padding(all = MaterialTheme.paddings.small).align(Alignment.Start)
        )
    }
}

@Composable
private fun rememberLauncherInfoCardStates(
    isAnyDebuggingSettingEnabled: Boolean,
    isVaultAvailable: Boolean
): List<InfoCardState> {
    val colors = MaterialTheme.colorScheme

    return remember(
        isAnyDebuggingSettingEnabled,
        isVaultAvailable
    ) {
        buildList {
            if (isAnyDebuggingSettingEnabled) {
                add(
                    InfoCardState(
                        title = R.string.info_card_debugging_title,
                        description = R.string.info_card_debugging_description,
                        icon = Icons.Default.Warning,
                        containerColor = colors.errorContainer,
                        contentColor = colors.onErrorContainer
                    )
                )
            }

            if (isVaultAvailable) {
                add(
                    InfoCardState(
                        title = R.string.info_card_backup_title,
                        description = R.string.info_card_backup_description,
                        icon = Icons.Outlined.CloudUpload,
                        containerColor = colors.primaryContainer,
                        contentColor = colors.onPrimaryContainer
                    )
                )

                add(
                    InfoCardState(
                        title = R.string.info_card_feedback_title,
                        description = R.string.info_card_feedback_description,
                        icon = Icons.Default.StarBorder,
                        containerColor = colors.secondaryContainer,
                        contentColor = colors.onSecondaryContainer
                    )
                )
            }
        }
    }
}

@Preview
@Composable
private fun LauncherInfoCardPreview(
    @PreviewParameter(BooleanPreviewParameterProvider::class) isDebuggingSettingsEnabled: Boolean
) = PreviewHost(Modifier) {
    LauncherInfoCardPager(
        isAnyDebuggingSettingEnabled = isDebuggingSettingsEnabled,
        isVaultAvailable = true
    )
}
