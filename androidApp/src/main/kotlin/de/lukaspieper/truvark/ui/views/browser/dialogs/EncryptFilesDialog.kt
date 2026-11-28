/*
 * SPDX-FileCopyrightText: 2024 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.ui.views.browser.dialogs

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import de.lukaspieper.truvark.R
import de.lukaspieper.truvark.ui.controls.MaterialDialog
import de.lukaspieper.truvark.ui.controls.SegmentedSwitchListItem
import de.lukaspieper.truvark.ui.preview.PagePreviews
import de.lukaspieper.truvark.ui.preview.PreviewHost
import de.lukaspieper.truvark.ui.views.ActivityResultContracts

@Composable
public fun EncryptFilesDialog(
    hideDialog: () -> Unit,
    encryptFiles: (List<Uri>, Boolean) -> Unit,
    encryptDirectory: (Uri, Boolean) -> Unit,
    forceDirectoryEncryption: Boolean,
    modifier: Modifier = Modifier
) {
    var deleteSourceFiles by rememberSaveable { mutableStateOf(false) }
    var selectDirectory by rememberSaveable { mutableStateOf(forceDirectoryEncryption) }

    val selectFilesLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocumentsWithFlags(),
        onResult = { uris ->
            hideDialog()
            encryptFiles(uris, deleteSourceFiles)
        }
    )
    val selectDirectoryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTreeWithFlags(),
        onResult = { uri ->
            hideDialog()

            if (uri != null) {
                encryptDirectory(uri, deleteSourceFiles)
            }
        }
    )

    MaterialDialog(
        modifier = modifier,
        onDismissRequest = { hideDialog() },
        title = R.string.encrypt_files,
        confirmButton = {
            Button(
                onClick = {
                    if (selectDirectory) {
                        selectDirectoryLauncher.launch(null)
                    } else {
                        selectFilesLauncher.launch(emptyArray())
                    }
                },
                content = { Text(stringResource(R.string.select)) }
            )
        },
        dismissButton = {
            OutlinedButton(
                onClick = { hideDialog() },
                content = { Text(stringResource(R.string.cancel)) }
            )
        }
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
            SegmentedSwitchListItem(
                text = stringResource(R.string.select_directory),
                checked = selectDirectory,
                onCheckedChange = { selectDirectory = it },
                enabled = forceDirectoryEncryption.not(),
                shapes = ListItemDefaults.segmentedShapes(index = 0, count = 2),
            )

            SegmentedSwitchListItem(
                text = stringResource(R.string.delete_source_files),
                checked = deleteSourceFiles,
                onCheckedChange = { deleteSourceFiles = it },
                shapes = ListItemDefaults.segmentedShapes(index = 1, count = 2),
                switchColors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onErrorContainer,
                    checkedTrackColor = MaterialTheme.colorScheme.errorContainer,
                    checkedIconColor = MaterialTheme.colorScheme.errorContainer
                )
            )
        }
    }
}

@PagePreviews
@Composable
private fun EncryptFilesDialogPreview() = PreviewHost {
    EncryptFilesDialog(
        hideDialog = {},
        encryptFiles = { _, _ -> },
        encryptDirectory = { _, _ -> },
        forceDirectoryEncryption = false
    )
}
