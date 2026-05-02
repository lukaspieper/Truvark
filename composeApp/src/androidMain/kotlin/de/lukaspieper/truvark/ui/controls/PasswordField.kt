/*
 * SPDX-FileCopyrightText: 2023 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.ui.controls

import androidx.annotation.StringRes
import androidx.compose.foundation.text.input.KeyboardActionHandler
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.TextObfuscationMode
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SecureTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import de.lukaspieper.truvark.R

@Composable
public fun PasswordField(
    state: TextFieldState,
    modifier: Modifier = Modifier,
    passwordIsIncorrect: Boolean = false,
    onKeyboardDone: KeyboardActionHandler? = null,
    @StringRes label: Int = R.string.enter_password,
    @StringRes incorrectPasswordText: Int = R.string.incorrect_password
) {
    var showPassword by remember { mutableStateOf(false) }

    SecureTextField(
        state = state,
        modifier = modifier,
        label = { Text(stringResource(label)) },
        isError = passwordIsIncorrect,
        supportingText = { if (passwordIsIncorrect) Text(stringResource(incorrectPasswordText)) },
        textObfuscationMode = when {
            showPassword -> TextObfuscationMode.Visible
            else -> TextObfuscationMode.Hidden
        },
        onKeyboardAction = onKeyboardDone,
        trailingIcon = {
            IconButton(onClick = { showPassword = !showPassword }) {
                val visibilityIcon = if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility
                Icon(imageVector = visibilityIcon, contentDescription = null)
            }
        }
    )
}
