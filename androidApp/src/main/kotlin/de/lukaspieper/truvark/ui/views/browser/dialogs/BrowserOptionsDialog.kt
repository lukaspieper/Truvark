/*
 * SPDX-FileCopyrightText: 2026 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.ui.views.browser.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SplitButtonDefaults
import androidx.compose.material3.SplitButtonLayout
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.lukaspieper.truvark.R
import de.lukaspieper.truvark.ui.controls.MaterialDialog
import de.lukaspieper.truvark.ui.controls.SingleLineText
import de.lukaspieper.truvark.ui.icons.MaterialSymbols
import de.lukaspieper.truvark.ui.preview.PagePreviews
import de.lukaspieper.truvark.ui.preview.PreviewHost
import de.lukaspieper.truvark.ui.theme.paddings
import de.lukaspieper.truvark.ui.views.browser.BrowserLayout
import de.lukaspieper.truvark.ui.views.browser.BrowserSortCriterion
import de.lukaspieper.truvark.ui.views.browser.BrowserSortDirection
import de.lukaspieper.truvark.ui.views.browser.BrowserSortState
import de.lukaspieper.truvark.ui.views.settings.SettingsSection

@Composable
internal fun BrowserOptionsDialog(
    hideDialog: () -> Unit,
    layout: BrowserLayout,
    updateLayout: (BrowserLayout) -> Unit,
    sortState: BrowserSortState,
    updateSortCriterion: (BrowserSortCriterion) -> Unit,
    updateSortDirection: (BrowserSortDirection) -> Unit,
    modifier: Modifier = Modifier
) {
    MaterialDialog(
        modifier = modifier,
        onDismissRequest = hideDialog,
        title = R.string.browser_options_title,
        titleAction = {
            IconButton(onClick = hideDialog) {
                Icon(
                    imageVector = MaterialSymbols.Close,
                    contentDescription = null
                )
            }
        },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.paddings.extraLarge)) {
            SettingsSection(title = stringResource(R.string.browser_options_layout)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    BrowserLayout.entries.forEachIndexed { index, option ->
                        val isSelected = option == layout

                        ToggleButton(
                            checked = isSelected,
                            onCheckedChange = { if (it) updateLayout(option) },
                            shapes = when (index) {
                                0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()

                                BrowserLayout.entries.lastIndex -> {
                                    ButtonGroupDefaults.connectedTrailingButtonShapes()
                                }

                                else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                            },
                            modifier = Modifier.weight(if (isSelected) 2f else 1f)
                        ) {
                            Icon(option.icon, null)

                            if (isSelected) {
                                Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
                                SingleLineText(text = stringResource(option.label))
                            }
                        }
                    }
                }
            }

            SettingsSection(title = stringResource(R.string.browser_options_sort_by)) {
                Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.paddings.medium)) {
                    BrowserSortSplitButton(criterion = sortState.criterion, updateSortCriterion = updateSortCriterion)
                    BrowserSortDirectionButtonGroup(
                        direction = sortState.direction,
                        updateSortDirection = updateSortDirection,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun BrowserSortSplitButton(
    criterion: BrowserSortCriterion,
    updateSortCriterion: (BrowserSortCriterion) -> Unit,
) {
    var isSortCriterionMenuExpanded by remember { mutableStateOf(false) }
    var anchorWidth by remember { mutableIntStateOf(0) }

    val density = LocalDensity.current
    val size = SplitButtonDefaults.MediumContainerHeight
    val colors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
    )

    Box(modifier = Modifier.fillMaxWidth()) {
        SplitButtonLayout(
            leadingButton = {
                SplitButtonDefaults.TonalLeadingButton(
                    onClick = { isSortCriterionMenuExpanded = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(size),
                    shapes = SplitButtonDefaults.leadingButtonShapesFor(size),
                    colors = colors,
                    contentPadding = SplitButtonDefaults.leadingButtonContentPaddingFor(size),
                ) {
                    Icon(
                        imageVector = criterion.icon,
                        contentDescription = null,
                        modifier = Modifier.size(SplitButtonDefaults.leadingButtonIconSizeFor(size))
                    )
                    Spacer(Modifier.size(ButtonDefaults.iconSpacingFor(size)))
                    SingleLineText(
                        text = stringResource(criterion.label),
                        modifier = Modifier.weight(1f),
                        style = ButtonDefaults.textStyleFor(size),
                    )
                }
            },
            trailingButton = {
                SplitButtonDefaults.TonalTrailingButton(
                    checked = isSortCriterionMenuExpanded,
                    onCheckedChange = { isSortCriterionMenuExpanded = it },
                    modifier = Modifier.heightIn(size),
                    shapes = SplitButtonDefaults.trailingButtonShapesFor(size),
                    colors = colors,
                    contentPadding = SplitButtonDefaults.trailingButtonContentPaddingFor(size),
                ) {
                    Icon(
                        imageVector = MaterialSymbols.ArrowDropDown,
                        contentDescription = null,
                        modifier = Modifier.size(SplitButtonDefaults.trailingButtonIconSizeFor(size))
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { anchorWidth = it.size.width }
        )

        DropdownMenu(
            expanded = isSortCriterionMenuExpanded,
            onDismissRequest = { isSortCriterionMenuExpanded = false },
            modifier = Modifier.width(with(density) { anchorWidth.toDp() })
        ) {
            BrowserSortCriterion.entries.forEach { option ->
                DropdownMenuItem(
                    text = { Text(stringResource(option.label)) },
                    onClick = {
                        updateSortCriterion(option)
                        isSortCriterionMenuExpanded = false
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = option.icon,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = if (option == criterion) {
                        {
                            Icon(
                                imageVector = MaterialSymbols.Check,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    } else {
                        null
                    },
                )
            }
        }
    }
}

@Composable
private fun BrowserSortDirectionButtonGroup(
    direction: BrowserSortDirection,
    updateSortDirection: (BrowserSortDirection) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
        modifier = modifier,
    ) {
        ToggleButton(
            checked = direction == BrowserSortDirection.ASCENDING,
            onCheckedChange = { if (it) updateSortDirection(BrowserSortDirection.ASCENDING) },
            shapes = ButtonGroupDefaults.connectedLeadingButtonShapes(),
            modifier = Modifier.weight(if (direction == BrowserSortDirection.ASCENDING) 2f else 1f)
        ) {
            Icon(
                imageVector = MaterialSymbols.ListArrow,
                contentDescription = null,
                modifier = Modifier.graphicsLayer(scaleY = -1f)
            )

            if (direction == BrowserSortDirection.ASCENDING) {
                Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
                SingleLineText(stringResource(R.string.browser_sort_ascending))
            }
        }
        ToggleButton(
            checked = direction == BrowserSortDirection.DESCENDING,
            onCheckedChange = { if (it) updateSortDirection(BrowserSortDirection.DESCENDING) },
            shapes = ButtonGroupDefaults.connectedTrailingButtonShapes(),
            modifier = Modifier.weight(if (direction == BrowserSortDirection.DESCENDING) 2f else 1f)
        ) {
            Icon(MaterialSymbols.ListArrow, null)

            if (direction == BrowserSortDirection.DESCENDING) {
                Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
                SingleLineText(stringResource(R.string.browser_sort_descending))
            }
        }
    }
}

@PagePreviews
@Composable
private fun BrowserOptionsDialogPreview() = PreviewHost {
    BrowserOptionsDialog(
        hideDialog = {},
        layout = BrowserLayout.LIST,
        updateLayout = {},
        sortState = BrowserSortState(),
        updateSortCriterion = {},
        updateSortDirection = {},
    )
}
