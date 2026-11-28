/*
 * SPDX-FileCopyrightText: 2026 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.ui.views.browser.dialogs

import android.net.Uri
import androidx.compose.runtime.Composable
import de.lukaspieper.truvark.ui.views.browser.BrowserLayout
import de.lukaspieper.truvark.ui.views.browser.BrowserSortCriterion
import de.lukaspieper.truvark.ui.views.browser.BrowserSortDirection
import de.lukaspieper.truvark.ui.views.browser.BrowserSortState

@Composable
internal fun BrowserDialogHost(
    visibleDialog: BrowserDialogs,
    hideDialog: () -> Unit,
    layout: BrowserLayout,
    folderName: String,
    isRootLevel: Boolean,
    createFolder: suspend (String) -> Boolean,
    renameFolder: suspend (String) -> Boolean,
    encryptFiles: (List<Uri>, Boolean) -> Unit,
    encryptDirectory: (Uri, Boolean) -> Unit,
    deleteSelectedCipherEntities: () -> Unit,
    updateLayout: (BrowserLayout) -> Unit,
    sortState: BrowserSortState,
    updateSortCriterion: (BrowserSortCriterion) -> Unit,
    updateSortDirection: (BrowserSortDirection) -> Unit,
) {
    when (visibleDialog) {
        BrowserDialogs.NONE -> Unit

        BrowserDialogs.BROWSER_OPTIONS -> BrowserOptionsDialog(
            hideDialog = hideDialog,
            layout = layout,
            updateLayout = updateLayout,
            sortState = sortState,
            updateSortCriterion = updateSortCriterion,
            updateSortDirection = updateSortDirection,
        )

        BrowserDialogs.NEW_FOLDER -> NewFolderDialog(
            hideDialog = hideDialog,
            createFolder = createFolder,
        )

        BrowserDialogs.RENAME_FOLDER -> RenameFolderDialog(
            hideDialog = hideDialog,
            folderName = folderName,
            renameFolder = renameFolder,
        )

        BrowserDialogs.ENCRYPT -> EncryptFilesDialog(
            hideDialog = hideDialog,
            encryptFiles = encryptFiles,
            encryptDirectory = encryptDirectory,
            forceDirectoryEncryption = isRootLevel,
        )

        BrowserDialogs.DELETE_SELECTION -> DeleteSelectionDialog(
            hideDialog = hideDialog,
            deleteSelectedCipherEntities = deleteSelectedCipherEntities,
        )
    }
}
