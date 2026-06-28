/*
 * SPDX-FileCopyrightText: 2024 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.ui.views.settings.vault

import androidx.biometric.BiometricManager
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.lukaspieper.truvark.R
import de.lukaspieper.truvark.ui.controls.PasswordField
import de.lukaspieper.truvark.ui.controls.SettingsSectionCard
import de.lukaspieper.truvark.ui.preview.ElementPreviews
import de.lukaspieper.truvark.ui.preview.PreviewHost
import de.lukaspieper.truvark.ui.theme.paddings
import de.lukaspieper.truvark.ui.views.settings.vault.VaultSettingsViewModel.BiometricSetupResult
import kotlinx.coroutines.launch

@Composable
public fun BiometricsView(
    biometricsStatus: Int,
    isVaultUsingBiometricUnlocking: Boolean,
    setupBiometricUnlocking: suspend (ByteArray) -> BiometricSetupResult,
    modifier: Modifier = Modifier
) {
    if (biometricsStatus == BiometricManager.BIOMETRIC_SUCCESS) {
        SettingsSectionCard(
            title = stringResource(R.string.biometric_unlocking),
            modifier = modifier
        ) {
            Column(
                verticalArrangement = spacedBy(MaterialTheme.paddings.medium),
            ) {
                if (isVaultUsingBiometricUnlocking) {
                    ActiveBiometricsIndicator()
                }

                Text(
                    text = stringResource(R.string.setup_biometrics_description),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Justify
                )

                SetupBiometricUnlockingView(setupBiometricUnlocking)
            }
        }
    }
}

@Composable
private fun SetupBiometricUnlockingView(
    setupBiometricUnlocking: suspend (ByteArray) -> BiometricSetupResult,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val passwordState = remember { TextFieldState() }

    var setupResult by remember { mutableStateOf<BiometricSetupResult?>(null) }
    val isPasswordIncorrect by remember(setupResult) {
        derivedStateOf { setupResult == BiometricSetupResult.InvalidPassword }
    }

    LaunchedEffect(setupResult) {
        if (setupResult == BiometricSetupResult.Success) {
            passwordState.clearText()
        }
    }

    Row(
        horizontalArrangement = spacedBy(MaterialTheme.paddings.medium),
        modifier = modifier.fillMaxWidth()
    ) {
        PasswordField(
            state = passwordState,
            label = R.string.confirm_vaults_password,
            onKeyboardDone = {
                coroutineScope.launch {
                    setupResult = setupBiometricUnlocking(passwordState.text.toString().toByteArray())
                }
            },
            passwordIsIncorrect = isPasswordIncorrect,
            modifier = Modifier.weight(1f)
        )

        Button(
            onClick = {
                coroutineScope.launch {
                    setupResult = setupBiometricUnlocking(passwordState.text.toString().toByteArray())
                }
            },
            modifier = Modifier.height(TextFieldDefaults.MinHeight)
        ) {
            Icon(Icons.Default.LockOpen, null)
        }
    }
}

@Composable
private fun ActiveBiometricsIndicator() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = MaterialTheme.shapes.large,
    ) {
        Row(
            horizontalArrangement = spacedBy(MaterialTheme.paddings.medium),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(MaterialTheme.paddings.large)
        ) {
            Icon(
                Icons.Outlined.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.requiredSize(24.dp)
            )

            Text(
                text = stringResource(R.string.vault_using_biometric_unlocking),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

@ElementPreviews
@Composable
private fun BiometricsViewPreview() = PreviewHost(Modifier) {
    BiometricsView(
        biometricsStatus = BiometricManager.BIOMETRIC_SUCCESS,
        isVaultUsingBiometricUnlocking = true,
        setupBiometricUnlocking = { BiometricSetupResult.Success }
    )
}
