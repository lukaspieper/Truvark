/*
 * SPDX-FileCopyrightText: 2024 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.ui.views.browser.dialogs

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import de.lukaspieper.truvark.R
import de.lukaspieper.truvark.ui.controls.MaterialDialog
import de.lukaspieper.truvark.ui.preview.PagePreviews
import de.lukaspieper.truvark.ui.preview.PreviewHost
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@OptIn(DelicateCoroutinesApi::class)
@Composable
public fun NewFolderDialog(
    hideDialog: () -> Unit,
    createFolder: suspend (String) -> Boolean,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }
    var folderName by rememberSaveable { mutableStateOf("") }
    var isInputValid by rememberSaveable { mutableStateOf(true) }

    MaterialDialog(
        modifier = modifier,
        onDismissRequest = { hideDialog() },
        confirmButton = {
            Button(onClick = {
                GlobalScope.launch {
                    isInputValid = createFolder(folderName)
                    if (isInputValid) hideDialog()
                }
            }) {
                Text(text = stringResource(R.string.create))
            }
        },
        dismissButton = {
            TextButton(onClick = { hideDialog() }) {
                Text(text = stringResource(R.string.cancel))
            }
        },
        title = R.string.create_new_folder
    ) {
        OutlinedTextField(
            value = folderName,
            onValueChange = { folderName = it },
            label = { Text(stringResource(R.string.folder_name)) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                GlobalScope.launch {
                    isInputValid = createFolder(folderName)
                    if (isInputValid) hideDialog()
                }
            }),
            isError = isInputValid.not(),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
        )

        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }
    }
}

@PagePreviews
@Composable
private fun NewFolderDialogPreview() = PreviewHost {
    NewFolderDialog(
        hideDialog = {},
        createFolder = { true }
    )
}

@OptIn(DelicateCoroutinesApi::class)
@Composable
public fun RenameFolderDialog(
    hideDialog: () -> Unit,
    folderName: String,
    renameFolder: suspend (String) -> Boolean,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }
    var editableFolderName by remember(folderName) {
        mutableStateOf(
            TextFieldValue(
                text = folderName,
                selection = TextRange(folderName.length)
            )
        )
    }
    var isInputValid by rememberSaveable { mutableStateOf(true) }

    MaterialDialog(
        modifier = modifier,
        onDismissRequest = { hideDialog() },
        confirmButton = {
            Button(onClick = {
                GlobalScope.launch {
                    isInputValid = renameFolder(editableFolderName.text)
                    if (isInputValid) hideDialog()
                }
            }) {
                Text(text = stringResource(R.string.rename))
            }
        },
        dismissButton = {
            TextButton(onClick = { hideDialog() }) {
                Text(text = stringResource(R.string.cancel))
            }
        },
        title = R.string.rename_folder
    ) {
        OutlinedTextField(
            value = editableFolderName,
            onValueChange = { editableFolderName = it },
            label = { Text(stringResource(R.string.folder_name)) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                GlobalScope.launch {
                    isInputValid = renameFolder(editableFolderName.text)
                    if (isInputValid) hideDialog()
                }
            }),
            isError = isInputValid.not(),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
        )

        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }
    }
}

@PagePreviews
@Composable
private fun RenameFolderDialogPreview() = PreviewHost {
    RenameFolderDialog(
        hideDialog = {},
        folderName = "Preview Folder",
        renameFolder = { true }
    )
}
