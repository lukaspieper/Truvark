/*
 * SPDX-FileCopyrightText: 2023 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.io.File
import javax.swing.JFileChooser

@Composable
public fun AppTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) darkColorScheme() else lightColorScheme(),
        typography = typography,
        shapes = shapes,
        content = content
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun MainView(viewModel: MainViewModel) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val openDialog by rememberSaveable { derivedStateOf { viewModel.vault == null } }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = viewModel.vault?.displayName ?: "Truvark",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                actions = {
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = null
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) {
        // TODO
    }

    if (openDialog) {
        AlertDialog(
            onDismissRequest = {
                // This dialog cannot be dismissed by pressing outside
            }
        ) {
            Surface(
                modifier = Modifier
                    .wrapContentWidth()
                    .wrapContentHeight(),
                shape = shapes.large,
                tonalElevation = AlertDialogDefaults.TonalElevation
            ) {
                if (viewModel.directory == null) {
                    DirectorySelection(viewModel)
                } else if (viewModel.vaultConfig == null) {
                    VaultCreation(viewModel)
                } else {
                    VaultUnlocking(viewModel)
                }
            }
        }
    }
}

@Composable
public fun DirectorySelection(viewModel: MainViewModel) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Open an existing vault or choose an empty directory to create a new vault.")
        Spacer(modifier = Modifier.height(24.dp))
        TextButton(
            onClick = {
                JFileChooser().apply {
                    currentDirectory = File(".")
                    dialogTitle = "Select a directory"
                    fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
                    isAcceptAllFileFilterUsed = false

                    if (showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                        viewModel.inspectDirectory(selectedFile)
                    }
                }
            },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Select")
        }
    }
}

@Composable
public fun VaultCreation(viewModel: MainViewModel) {
    Column(modifier = Modifier.padding(16.dp)) {
        var password by rememberSaveable { mutableStateOf("") }
        PasswordField(password, { password = it })

        Spacer(modifier = Modifier.height(12.dp))

        var passwordConfirmation by rememberSaveable { mutableStateOf("") }
        PasswordField(passwordConfirmation, { passwordConfirmation = it }, "Confirm password")

        Spacer(modifier = Modifier.height(24.dp))

        TextButton(
            onClick = {
                if (password == passwordConfirmation) {
                    viewModel.createVault(password)
                }
            },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Create vault")
        }
    }
}

@Composable
public fun VaultUnlocking(viewModel: MainViewModel) {
    Column(modifier = Modifier.padding(16.dp)) {
        var password by rememberSaveable { mutableStateOf("") }
        PasswordField(password, { password = it })

        Spacer(modifier = Modifier.height(24.dp))

        TextButton(
            onClick = {
                viewModel.unlockVault(password)
            },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Unlock vault")
        }
    }
}

@Composable
public fun PasswordField(value: String, onValueChange: (String) -> Unit, label: String = "Enter password") {
    var passwordHidden by rememberSaveable { mutableStateOf(true) }
    TextField(
        value = value,
        onValueChange = { onValueChange(it) },
        singleLine = true,
        label = { Text(label) },
        visualTransformation = if (passwordHidden) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        trailingIcon = {
            IconButton(onClick = { passwordHidden = !passwordHidden }) {
                val visibilityIcon =
                    if (passwordHidden) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                // Please provide localized description for accessibility services
                val description = if (passwordHidden) "Show password" else "Hide password"
                Icon(imageVector = visibilityIcon, contentDescription = description)
            }
        }
    )
}
