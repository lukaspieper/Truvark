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
import de.lukaspieper.truvark.domain.entities.CipherFileEntity
import de.lukaspieper.truvark.domain.entities.CipherFolderEntity

public class SelectionState(
    initialSelectedFolders: Set<CipherFolderEntity> = emptySet(),
    initialSelectedFiles: Set<CipherFileEntity> = emptySet(),
) {
    public var relocationSourceFolder: CipherFolderEntity? by mutableStateOf(null)
        private set

    public var selectedFolders: Set<CipherFolderEntity> by mutableStateOf(initialSelectedFolders)
        private set

    public var selectedFiles: Set<CipherFileEntity> by mutableStateOf(initialSelectedFiles)
        private set

    public val numberOfSelections: Int by derivedStateOf { selectedFolders.size + selectedFiles.size }

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
        selectedFolders = emptySet()
        selectedFiles = emptySet()
        relocationSourceFolder = null
    }

    public fun enableRelocationMode(sourceFolder: CipherFolderEntity) {
        relocationSourceFolder = sourceFolder
    }

    public fun selectFolders(folders: Collection<CipherFolderEntity>) {
        if (folders.isEmpty()) return
        selectedFolders = selectedFolders + folders
    }

    public fun deselectFolders(folders: Collection<CipherFolderEntity>) {
        if (folders.isEmpty()) return
        selectedFolders = selectedFolders - folders.toSet()
    }

    public fun selectFiles(files: Collection<CipherFileEntity>) {
        if (files.isEmpty()) return
        selectedFiles = selectedFiles + files
    }

    public fun deselectFiles(files: Collection<CipherFileEntity>) {
        if (files.isEmpty()) return
        selectedFiles = selectedFiles - files.toSet()
    }

    public fun switchFolderSelection(folder: CipherFolderEntity) {
        selectedFolders = when {
            selectedFolders.contains(folder) -> selectedFolders - folder
            else -> selectedFolders + folder
        }
    }

    public fun switchFileSelection(file: CipherFileEntity) {
        selectedFiles = when {
            selectedFiles.contains(file) -> selectedFiles - file
            else -> selectedFiles + file
        }
    }

    public enum class SelectionMode {
        NONE,
        SELECTION,
        RELOCATION
    }
}
