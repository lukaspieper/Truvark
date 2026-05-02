/*
 * SPDX-FileCopyrightText: 2024 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.ui.views.browser

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import de.lukaspieper.truvark.domain.entities.CipherFolderEntity

public class SelectionState(
    initialSelectedFolderIds: Set<String> = emptySet(),
    initialSelectedFileIds: Set<String> = emptySet(),
) {
    public var relocationSourceFolder: CipherFolderEntity? by mutableStateOf(null)
        private set

    public var selectedFolderIds: Set<String> by mutableStateOf(initialSelectedFolderIds)
        private set

    public var selectedFileIds: Set<String> by mutableStateOf(initialSelectedFileIds)
        private set

    public val numberOfSelections: Int by derivedStateOf { selectedFolderIds.size + selectedFileIds.size }

    public val mode: SelectionMode by derivedStateOf {
        when {
            relocationSourceFolder != null -> SelectionMode.RELOCATION
            numberOfSelections > 0 -> SelectionMode.SELECTION
            else -> SelectionMode.NONE
        }
    }

    /**
     * Switches the selection mode to [SelectionMode.NONE].
     */
    public fun disableSelectionMode() {
        selectedFolderIds = emptySet()
        selectedFileIds = emptySet()
        relocationSourceFolder = null
    }

    public fun enableRelocationMode(sourceFolder: CipherFolderEntity) {
        relocationSourceFolder = sourceFolder
    }

    public fun selectFolders(folderIds: Set<String>) {
        if (folderIds.isEmpty()) return
        selectedFolderIds = selectedFolderIds + folderIds
    }

    public fun deselectFolders(folderIds: Set<String>) {
        if (folderIds.isEmpty()) return
        selectedFolderIds = selectedFolderIds - folderIds
    }

    public fun selectFiles(fileIds: Set<String>) {
        if (fileIds.isEmpty()) return
        selectedFileIds = selectedFileIds + fileIds
    }

    public fun deselectFiles(fileIds: Set<String>) {
        if (fileIds.isEmpty()) return
        selectedFileIds = selectedFileIds - fileIds
    }

    public fun switchFolderSelection(folderId: String) {
        selectedFolderIds = when {
            selectedFolderIds.contains(folderId) -> selectedFolderIds - folderId
            else -> selectedFolderIds + folderId
        }
    }

    public fun switchFileSelection(fileId: String) {
        selectedFileIds = when {
            selectedFileIds.contains(fileId) -> selectedFileIds - fileId
            else -> selectedFileIds + fileId
        }
    }

    public enum class SelectionMode {
        NONE,
        SELECTION,
        RELOCATION
    }
}
