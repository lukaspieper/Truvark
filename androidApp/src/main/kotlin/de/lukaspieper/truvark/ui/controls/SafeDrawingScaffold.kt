/*
 * SPDX-FileCopyrightText: 2024 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.ui.controls

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.plus
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import de.lukaspieper.truvark.ui.theme.paddings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun SafeDrawingScaffold(
    largeTopAppBarTitle: String,
    modifier: Modifier = Modifier,
    largeTopAppBarActions: @Composable RowScope.() -> Unit = {},
    largeTopAppBarNavigationIcon: @Composable () -> Unit = {},
    bottomOverlay: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = modifier
            .fillMaxSize() // https://stackoverflow.com/a/76916130
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        text = largeTopAppBarTitle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                scrollBehavior = scrollBehavior,
                actions = {
                    Row {
                        largeTopAppBarActions()
                    }
                },
                navigationIcon = largeTopAppBarNavigationIcon
            )
        },
        content = {
            Box {
                val paddingValues = it + PaddingValues(MaterialTheme.paddings.large)
                content(paddingValues)

                Box(
                    contentAlignment = Alignment.BottomEnd,
                    modifier = Modifier
                        .matchParentSize()
                        .padding(paddingValues)
                ) {
                    bottomOverlay()
                }
            }
        }
    )
}
