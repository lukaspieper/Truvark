/*
 * SPDX-FileCopyrightText: 2026 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.ui.views.launcher

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.shouldShowRationale
import de.lukaspieper.truvark.R
import de.lukaspieper.truvark.ui.controls.ShapedIcon
import de.lukaspieper.truvark.ui.theme.paddings

@OptIn(ExperimentalPermissionsApi::class)
@Composable
internal fun NotificationPermissionView(notificationPermissionState: PermissionState, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var requestPermissionCounter by rememberSaveable { mutableIntStateOf(0) }
    var permissionRequestCompleted by rememberSaveable { mutableStateOf(false) }

    with(notificationPermissionState) {
        LaunchedEffect(status) {
            if (requestPermissionCounter > 0) {
                permissionRequestCompleted = true
            }
        }

        val navigateToSettings by remember {
            derivedStateOf {
                requestPermissionCounter > 1 || (permissionRequestCompleted && !status.shouldShowRationale)
            }
        }

        Card(
            shape = MaterialTheme.shapes.extraLarge,
            modifier = modifier.sizeIn(maxWidth = 550.dp)
        ) {
            Column(
                modifier = Modifier.padding(all = MaterialTheme.paddings.large),
                verticalArrangement = spacedBy(MaterialTheme.paddings.large),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ShapedIcon(
                    imageVector = Icons.Default.Notifications,
                    tint = MaterialTheme.colorScheme.primary,
                    shape = MaterialShapes.Sunny
                )

                Column(
                    verticalArrangement = spacedBy(MaterialTheme.paddings.small),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.notification_permission_title),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = stringResource(R.string.notification_permission_description),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = {
                        if (navigateToSettings) {
                            context.startActivity(
                                Intent().apply {
                                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                                    data = Uri.fromParts("package", context.packageName, null)
                                }
                            )
                        } else {
                            launchPermissionRequest()
                            requestPermissionCounter++
                        }
                    },
                    shape = MaterialTheme.shapes.extraLarge,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = if (navigateToSettings) Icons.Default.Settings else Icons.Default.Notifications,
                        contentDescription = null,
                        modifier = Modifier.size(ButtonDefaults.IconSize)
                    )
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text(
                        text = if (navigateToSettings) {
                            stringResource(R.string.open_app_settings)
                        } else {
                            stringResource(R.string.grant_permission)
                        }
                    )
                }
            }
        }
    }
}
