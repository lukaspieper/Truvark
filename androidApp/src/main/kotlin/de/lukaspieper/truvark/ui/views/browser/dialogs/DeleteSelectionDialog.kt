/*
 * SPDX-FileCopyrightText: 2024 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.ui.views.browser.dialogs

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import de.lukaspieper.truvark.R
import de.lukaspieper.truvark.ui.controls.MaterialDialog
import de.lukaspieper.truvark.ui.preview.PagePreviews
import de.lukaspieper.truvark.ui.preview.PreviewHost

@Composable
public fun DeleteSelectionDialog(
    hideDialog: () -> Unit,
    deleteSelectedCipherEntities: () -> Unit,
    modifier: Modifier = Modifier
) {
    MaterialDialog(
        modifier = modifier,
        onDismissRequest = { hideDialog() },
        title = R.string.confirm_deletion,
        confirmButton = {
            Button(
                onClick = {
                    deleteSelectedCipherEntities()
                    hideDialog()
                },
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
                content = { Text(stringResource(R.string.delete)) }
            )
        },
        dismissButton = {
            OutlinedButton(
                onClick = { hideDialog() },
                content = { Text(stringResource(R.string.cancel)) }
            )
        }
    ) {
        Text(stringResource(R.string.delete_warning))
    }
}

@PagePreviews
@Composable
private fun DeleteSelectionDialogPreview() = PreviewHost {
    DeleteSelectionDialog(
        hideDialog = {},
        deleteSelectedCipherEntities = {}
    )
}
