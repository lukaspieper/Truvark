/*
 * SPDX-FileCopyrightText: 2022 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.ui.views.launcher

import android.Manifest
import android.content.res.Configuration
import android.os.Build
import androidx.activity.compose.LocalActivity
import androidx.annotation.StringRes
import androidx.biometric.AuthenticationRequest
import androidx.biometric.AuthenticationRequest.Biometric.Strength
import androidx.biometric.AuthenticationResult
import androidx.biometric.BiometricPrompt
import androidx.biometric.compose.rememberAuthenticationLauncher
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import de.lukaspieper.truvark.ListPaneRoute
import de.lukaspieper.truvark.R
import de.lukaspieper.truvark.Route
import de.lukaspieper.truvark.SinglePaneRoute
import de.lukaspieper.truvark.ui.controls.PageIndicator
import de.lukaspieper.truvark.ui.controls.PasswordField
import de.lukaspieper.truvark.ui.controls.SafeDrawingScaffold
import de.lukaspieper.truvark.ui.controls.ShapedImage
import de.lukaspieper.truvark.ui.controls.SingleLineText
import de.lukaspieper.truvark.ui.preview.PagePreviews
import de.lukaspieper.truvark.ui.preview.PreviewHost
import de.lukaspieper.truvark.ui.theme.paddings
import de.lukaspieper.truvark.ui.views.launcher.LauncherViewModel.LauncherState.DirectorySelection
import de.lukaspieper.truvark.ui.views.launcher.LauncherViewModel.LauncherState.Processing
import de.lukaspieper.truvark.ui.views.launcher.LauncherViewModel.LauncherState.Ready
import de.lukaspieper.truvark.ui.views.launcher.LauncherViewModel.LauncherState.VaultCreation
import de.lukaspieper.truvark.ui.views.launcher.LauncherViewModel.LauncherState.VaultUnlocked
import logcat.LogPriority
import logcat.asLog
import logcat.logcat
import kotlin.uuid.Uuid

@OptIn(ExperimentalPermissionsApi::class)
@Composable
public fun LauncherPage(
    navigateAndClearBackStack: (Route) -> Unit,
    navigateTo: (Route) -> Unit,
    viewModel: LauncherViewModel,
    modifier: Modifier = Modifier
) {
    val activity = LocalActivity.current!!

    val vaults by viewModel.vaultEntries.collectAsStateWithLifecycle()
    val launcherState by viewModel.state.collectAsStateWithLifecycle()

    var biometricVault by remember { mutableStateOf<LauncherViewModel.RecentVaultInfo?>(null) }

    LaunchedEffect(launcherState, navigateAndClearBackStack) {
        (launcherState as? VaultUnlocked)?.let { state ->
            navigateAndClearBackStack(SinglePaneRoute.Browser(state.vaultId))
        }
    }

    val notificationPermissionState = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
            rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
        }

        else -> null
    }

    val authLauncher = rememberAuthenticationLauncher(resultCallback = { result ->
        when (result) {
            is AuthenticationResult.Success -> {
                result.crypto?.cipher?.let { cipher ->
                    biometricVault?.let { viewModel.unlockWithCipher(it, cipher) }
                }
            }

            is AuthenticationResult.Error -> {
                logcat("LauncherPage", LogPriority.WARN) {
                    "Biometric unlocking failed: ${result.errorCode} '${result.errString}'"
                }

                val userCausedErrors = listOf(
                    BiometricPrompt.ERROR_NEGATIVE_BUTTON,
                    BiometricPrompt.ERROR_USER_CANCELED,
                    BiometricPrompt.ERROR_CANCELED
                )
                if (result.errorCode !in userCausedErrors) {
                    biometricVault?.let(viewModel::disableBiometricUnlockingBecauseOfError)
                }
            }

            else -> {}
        }
    })

    val isAnyDebuggingSettingEnabled = viewModel.isAnyDebuggingSettingEnabled.collectAsStateWithLifecycle(false)
    LauncherView(
        notificationPermissionState = notificationPermissionState,
        state = launcherState,
        updateState = { viewModel.state.value = it },
        vaults = vaults,
        unlockVaultWithPassword = viewModel::unlockVaultWithPassword,
        navigateToSettings = { navigateTo(ListPaneRoute.SettingsHome(vaultId = null)) },
        showBiometricPrompt = { vault ->
            biometricVault = vault
            try {
                authLauncher.launch(
                    AuthenticationRequest.Biometric.Builder(title = activity.getString(R.string.biometric_unlocking))
                        .setMinStrength(Strength.Class3(viewModel.getCryptoObject(vault)))
                        .setIsConfirmationRequired(true)
                        .build()
                )
            } catch (e: Exception) {
                logcat("LauncherPage", LogPriority.ERROR) { e.asLog() }
                viewModel.disableBiometricUnlockingBecauseOfError(vault)
            }
        },
        setupDialog = {
            SetupDialog(
                state = launcherState,
                dismissDialog = { viewModel.state.value = Ready() },
                inspectDirectory = viewModel::inspectDirectory,
                createVault = viewModel::createVault
            )
        },
        clearRecentVaultHistory = viewModel::clearRecentVaultHistory,
        isAnyDebuggingSettingEnabled = isAnyDebuggingSettingEnabled.value,
        modifier = modifier
    )
}

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun LauncherView(
    notificationPermissionState: PermissionState?,
    state: LauncherViewModel.LauncherState,
    vaults: List<LauncherViewModel.RecentVaultInfo>,
    updateState: (LauncherViewModel.LauncherState) -> Unit,
    unlockVaultWithPassword: (LauncherViewModel.RecentVaultInfo, ByteArray) -> Unit,
    navigateToSettings: () -> Unit,
    showBiometricPrompt: (LauncherViewModel.RecentVaultInfo) -> Unit,
    setupDialog: @Composable () -> Unit,
    isAnyDebuggingSettingEnabled: Boolean,
    modifier: Modifier = Modifier,
    clearRecentVaultHistory: () -> Unit = {}
) {
    SafeDrawingScaffold(
        largeTopAppBarTitle = stringResource(R.string.app_name),
        largeTopAppBarActions = {
            IconButton(
                onClick = navigateToSettings,
                content = { Icon(Icons.Default.Settings, null) }
            )
        },
        modifier = modifier,
    ) { paddingValues ->
        AdaptivePane(
            firstPane = {
                LauncherInfoCardPager(
                    isAnyDebuggingSettingEnabled = isAnyDebuggingSettingEnabled,
                    isVaultAvailable = vaults.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            secondPane = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .imePadding(),
                    verticalArrangement = spacedBy(MaterialTheme.paddings.extraLarge),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (notificationPermissionState?.status is PermissionStatus.Denied) {
                        NotificationPermissionView(notificationPermissionState)
                    } else {
                        if (vaults.isNotEmpty()) {
                            VaultUnlockCardPager(
                                vaults = vaults,
                                unlockVaultWithPassword = unlockVaultWithPassword,
                                showBiometricPrompt = showBiometricPrompt,
                                clearRecentVaultHistory = clearRecentVaultHistory,
                                vaultPreselectionId = (state as? Ready)?.vaultPreselectionId,
                                modifier = Modifier.sizeIn(maxWidth = 550.dp)
                            )
                        } else {
                            NoVaultCardView()
                        }

                        val size = ButtonDefaults.MediumContainerHeight
                        FilledTonalButton(
                            onClick = { updateState(DirectorySelection) },
                            modifier = Modifier
                                .heightIn(size)
                                .width(550.dp),
                            contentPadding = ButtonDefaults.contentPaddingFor(size, hasStartIcon = true),
                        ) {
                            Icon(
                                Icons.Outlined.FolderOpen,
                                contentDescription = null,
                                modifier = Modifier.size(ButtonDefaults.iconSizeFor(size)),
                            )
                            Spacer(Modifier.size(ButtonDefaults.iconSpacingFor(size)))
                            Text(
                                stringResource(R.string.create_or_open_vault),
                                style = ButtonDefaults.textStyleFor(size)
                            )
                        }

                        if (state is DirectorySelection || state is VaultCreation || state is Processing) {
                            setupDialog()
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
        )
    }
}

@Composable
private fun AdaptivePane(
    firstPane: @Composable () -> Unit,
    secondPane: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val firstPaneContent = remember { movableContentOf { firstPane() } }
    val secondPaneContent = remember { movableContentOf { secondPane() } }

    if (isLandscape) {
        Row(
            horizontalArrangement = spacedBy(MaterialTheme.paddings.extraLarge),
            modifier = modifier
        ) {
            Box(Modifier.weight(0.4f)) { firstPaneContent() }
            Box(
                Modifier
                    .weight(0.6f)
                    .align(Alignment.CenterVertically)
            ) { secondPaneContent() }
        }
    } else {
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = modifier
        ) {
            firstPaneContent()
            secondPaneContent()
        }
    }
}

@Composable
private fun NoVaultCardView(modifier: Modifier = Modifier) {
    Card(
        shape = MaterialTheme.shapes.extraLarge,
        modifier = modifier.sizeIn(maxWidth = 550.dp)
    ) {
        Column(
            verticalArrangement = spacedBy(MaterialTheme.paddings.large),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(MaterialTheme.paddings.large)
        ) {
            ShapedImage(
                painter = painterResource(R.drawable.ic_locker),
                background = MaterialTheme.colorScheme.primaryContainer,
                shape = MaterialShapes.Cookie12Sided
            )

            Column(
                verticalArrangement = spacedBy(MaterialTheme.paddings.small),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.no_vault_found_title),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = stringResource(R.string.no_existing_vault_info),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun VaultUnlockCardPager(
    vaults: List<LauncherViewModel.RecentVaultInfo>,
    vaultPreselectionId: Uuid?,
    unlockVaultWithPassword: (LauncherViewModel.RecentVaultInfo, ByteArray) -> Unit,
    showBiometricPrompt: (LauncherViewModel.RecentVaultInfo) -> Unit,
    clearRecentVaultHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = { vaults.size })

    LaunchedEffect(vaults, vaultPreselectionId) {
        if (vaultPreselectionId != null) {
            val index = vaults.indexOfFirst { it.config.id == vaultPreselectionId }.coerceAtLeast(0)
            pagerState.scrollToPage(index)
        }
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .padding(all = MaterialTheme.paddings.extraSmall)
                .align(Alignment.End),
            horizontalArrangement = spacedBy(MaterialTheme.paddings.small),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PageIndicator(
                pagerState = pagerState,
                itemSize = vaults.size,
                modifier = Modifier
            )

            OutlinedButton(onClick = clearRecentVaultHistory,) {
                Text(stringResource(R.string.clear))
            }
        }

        HorizontalPager(
            state = pagerState,
            key = { index -> vaults[index].config.id },
            pageSpacing = MaterialTheme.paddings.small,
            modifier = Modifier.fillMaxWidth()
        ) { index ->
            val vault = vaults[index]
            VaultUnlockCardView(
                vault = vault,
                biometricUnlockingSupported = vault.biometricUnlockAvailable && index == pagerState.currentPage,
                unlockingErrorText = vault.unlockingErrorText.takeIf { index == pagerState.currentPage },
                unlockVaultWithPassword = { password -> unlockVaultWithPassword(vault, password) },
                showBiometricPrompt = { showBiometricPrompt(vault) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun VaultUnlockCardView(
    vault: LauncherViewModel.RecentVaultInfo,
    biometricUnlockingSupported: Boolean,
    unlockingErrorText: Int?,
    unlockVaultWithPassword: (ByteArray) -> Unit,
    showBiometricPrompt: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = MaterialTheme.shapes.extraLarge,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = spacedBy(MaterialTheme.paddings.large),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .padding(MaterialTheme.paddings.extraLarge)
            ) {
                ShapedImage(
                    painter = painterResource(R.drawable.ic_locker),
                    background = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialShapes.Cookie12Sided
                )
                Spacer(Modifier.width(MaterialTheme.paddings.large))
                SingleLineText(
                    text = vault.config.name,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.headlineLarge
                )
            }

            Column(
                verticalArrangement = spacedBy(MaterialTheme.paddings.medium),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = MaterialTheme.paddings.extraLarge,
                        end = MaterialTheme.paddings.extraLarge,
                        bottom = MaterialTheme.paddings.extraLarge
                    )
            ) {
                PasswordUnlockView(unlockVaultWithPassword, unlockingErrorText)

                if (biometricUnlockingSupported) {
                    val size = ButtonDefaults.MediumContainerHeight
                    Button(
                        onClick = showBiometricPrompt,
                        modifier = Modifier
                            .heightIn(size)
                            .fillMaxWidth(),
                        contentPadding = ButtonDefaults.contentPaddingFor(size, hasStartIcon = true),
                    ) {
                        Icon(
                            Icons.Default.Fingerprint,
                            contentDescription = null,
                            modifier = Modifier.size(ButtonDefaults.iconSizeFor(size)),
                        )
                        Spacer(Modifier.size(ButtonDefaults.iconSpacingFor(size)))
                        Text(
                            stringResource(R.string.biometric_unlocking),
                            style = ButtonDefaults.textStyleFor(size)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PasswordUnlockView(
    unlockWithPassword: (ByteArray) -> Unit,
    @StringRes errorMessageResource: Int?
) {
    if (errorMessageResource != null && errorMessageResource != R.string.incorrect_password) {
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.errorContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = stringResource(errorMessageResource),
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(MaterialTheme.paddings.medium)
            )
        }
    }

    Row(
        horizontalArrangement = spacedBy(MaterialTheme.paddings.medium),
        modifier = Modifier.fillMaxWidth()
    ) {
        val keyboardController = LocalSoftwareKeyboardController.current
        val passwordState = remember { TextFieldState() }
        PasswordField(
            state = passwordState,
            onKeyboardDone = {
                keyboardController?.hide()
                unlockWithPassword(passwordState.text.toString().toByteArray())
            },
            passwordIsIncorrect = errorMessageResource == R.string.incorrect_password,
            modifier = Modifier.weight(1f)
        )

        Button(
            onClick = {
                keyboardController?.hide()
                unlockWithPassword(passwordState.text.toString().toByteArray())
            },
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .height(TextFieldDefaults.MinHeight)
        ) {
            Icon(Icons.Default.LockOpen, null)
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@PagePreviews
@Composable
private fun NoNotificationPermissionPreview() = PreviewHost {
    LauncherView(
        notificationPermissionState = object : PermissionState {
            override val permission: String = ""
            override val status: PermissionStatus = PermissionStatus.Denied(false)

            override fun launchPermissionRequest() {
                // Previews do not need implementations.
            }
        },
        state = Ready(),
        vaults = emptyList(),
        updateState = {},
        unlockVaultWithPassword = { _, _ -> },
        navigateToSettings = {},
        showBiometricPrompt = {},
        setupDialog = {},
        isAnyDebuggingSettingEnabled = true
    )
}

@OptIn(ExperimentalPermissionsApi::class)
@PagePreviews
@Composable
private fun NoVaultSelectedPreview() = PreviewHost {
    LauncherView(
        notificationPermissionState = null,
        state = Ready(),
        vaults = emptyList(),
        updateState = {},
        unlockVaultWithPassword = { _, _ -> },
        navigateToSettings = {},
        showBiometricPrompt = {},
        setupDialog = {},
        isAnyDebuggingSettingEnabled = false
    )
}
