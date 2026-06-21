/*
 * SPDX-FileCopyrightText: 2023 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.ui.views.launcher

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import de.lukaspieper.truvark.R
import de.lukaspieper.truvark.domain.vault.VaultConfig
import de.lukaspieper.truvark.ui.controls.MaterialDialog
import de.lukaspieper.truvark.ui.controls.PasswordField
import de.lukaspieper.truvark.ui.preview.PagePreviews
import de.lukaspieper.truvark.ui.preview.PreviewHost
import de.lukaspieper.truvark.ui.views.ActivityResultContracts
import de.lukaspieper.truvark.ui.views.launcher.LauncherViewModel.LauncherState.DIRECTORY_SELECTION
import de.lukaspieper.truvark.ui.views.launcher.LauncherViewModel.LauncherState.NONE
import de.lukaspieper.truvark.ui.views.launcher.LauncherViewModel.LauncherState.PROCESSING
import de.lukaspieper.truvark.ui.views.launcher.LauncherViewModel.LauncherState.VAULT_CREATION

@Composable
public fun SetupDialog(
    state: LauncherViewModel.LauncherState,
    updateState: (LauncherViewModel.LauncherState) -> Unit,
    inspectDirectory: (Uri) -> Unit,
    createVault: (ByteArray) -> Unit,
    modifier: Modifier = Modifier
) {
    if (state == PROCESSING) {
        MaterialDialog(
            isLoadingIndicator = true,
            modifier = modifier,
            content = {}
        )
    } else if (state == DIRECTORY_SELECTION) {
        val openDocumentTreeLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.OpenDocumentTreeWithFlags()
        ) { uri ->
            uri?.let { inspectDirectory(it) }
        }

        MaterialDialog(
            title = R.string.create_or_open_vault,
            modifier = modifier,
            dismissButton = {
                TextButton(
                    onClick = { updateState(NONE) },
                ) {
                    Text(stringResource(R.string.cancel))
                }
            },
            confirmButton = {
                Button(
                    onClick = { openDocumentTreeLauncher.launch(null) },
                ) {
                    Text(stringResource(R.string.choose_vault_root_dir))
                }
            }
        ) {
            Image(
                painter = painterResource(R.drawable.ic_vault_filesystem),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                contentDescription = null,
                modifier = Modifier
                    .height(200.dp)
                    .fillMaxWidth()
            )

            Text(
                text = stringResource(R.string.storage_location_text),
                textAlign = TextAlign.Justify
            )
        }
    } else if (state == VAULT_CREATION) {
        val passwordState = remember { TextFieldState() }
        val passwordConfirmationState = remember { TextFieldState() }
        var errorText by rememberSaveable { mutableStateOf<Int?>(null) }

        MaterialDialog(
            title = R.string.set_password,
            confirmButton = {
                Button(
                    onClick = {
                        if (passwordState.text != passwordConfirmationState.text) {
                            errorText = R.string.inputs_do_not_match
                        } else if (passwordState.text.length < VaultConfig.MIN_PASSWORD_LENGTH) {
                            errorText = R.string.password_length
                        } else {
                            createVault(passwordState.text.toString().toByteArray())
                        }
                    }
                ) {
                    Text(stringResource(R.string.create_vault))
                }
            }
        ) {
            Text(
                text = stringResource(R.string.setup_password_text),
                textAlign = TextAlign.Justify
            )

            PasswordField(
                state = passwordState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            )

            PasswordField(
                state = passwordConfirmationState,
                label = R.string.repeat_password,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = if (errorText != null) stringResource(errorText!!, VaultConfig.MIN_PASSWORD_LENGTH) else "",
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

private class LauncherStatePreviewParameterProvider : PreviewParameterProvider<LauncherViewModel.LauncherState> {
    override val values: Sequence<LauncherViewModel.LauncherState>
        get() = sequenceOf(DIRECTORY_SELECTION, VAULT_CREATION, PROCESSING)
}

@PagePreviews
@Composable
private fun SetupDialogPreview(
    @PreviewParameter(LauncherStatePreviewParameterProvider::class) state: LauncherViewModel.LauncherState
) = PreviewHost {
    SetupDialog(
        state = state,
        updateState = {},
        inspectDirectory = {},
        createVault = {}
    )
}
