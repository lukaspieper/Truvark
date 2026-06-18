/*
 * SPDX-FileCopyrightText: 2024 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.ui.views.browser

import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil3.ImageLoader
import de.lukaspieper.truvark.R
import de.lukaspieper.truvark.data.io.AndroidFileSystem
import de.lukaspieper.truvark.data.io.FileInfo
import de.lukaspieper.truvark.data.preferences.PersistentPreferences
import de.lukaspieper.truvark.domain.entities.CipherFileEntity
import de.lukaspieper.truvark.domain.entities.CipherFolderEntity
import de.lukaspieper.truvark.domain.vault.Vault
import de.lukaspieper.truvark.work.WorkScheduler
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import logcat.LogPriority
import logcat.asLog
import logcat.logcat
import kotlin.uuid.Uuid

public class BrowserViewModel(
    private val vault: Vault,
    private val fileSystem: AndroidFileSystem,
    private val preferences: PersistentPreferences,
    public val imageLoader: ImageLoader,
) : ViewModel() {
    private val backstack = mutableStateListOf(FolderHierarchyLevel(vault.rootFolder))
    private var updateFolderHierarchyLevelJob: Job

    public val currentFolderHierarchyLevel: FolderHierarchyLevel by derivedStateOf { backstack.last() }
    public val isRootLevel: Boolean by derivedStateOf { currentFolderHierarchyLevel.folder.id == vault.rootFolder.id }
    public val isListLayout: Flow<Boolean> = preferences.isListLayout

    public val selectionState: SelectionState = SelectionState()

    init {
        updateFolderHierarchyLevelJob = updateFolderHierarchyLevel()
    }

    public fun navigateToFolder(currentFolderId: Uuid, folder: CipherFolderEntity) {
        synchronized(backstack) {
            // Prevent parallel folders from being added to the navigation stack, happens with Multi-Touch.
            if (currentFolderId != currentFolderHierarchyLevel.folder.id) {
                return
            }

            updateFolderHierarchyLevelJob.cancel()
            backstack.add(FolderHierarchyLevel(folder))
        }

        updateFolderHierarchyLevelJob = updateFolderHierarchyLevel()
    }

    public fun navigateToParentFolder() {
        if (backstack.size <= 1) {
            return
        }

        synchronized(backstack) {
            updateFolderHierarchyLevelJob.cancel()
            if (selectionState.mode != SelectionState.SelectionMode.RELOCATION) {
                selectionState.disableSelectionMode()
            }

            backstack.removeAt(backstack.lastIndex)
        }

        updateFolderHierarchyLevelJob = updateFolderHierarchyLevel()
    }

    private fun updateFolderHierarchyLevel(): Job {
        // TODO: Optimize loading data for the grid/list view.
        // - throttling (conflate+delay) is needed to prevent updates before animations are finished.
        // - loading the physical files may still be to slow.

        val currentFolder = currentFolderHierarchyLevel.folder
        return viewModelScope.launch(Dispatchers.IO) {
            launch {
                vault.findCipherFolderEntitySubfolders(currentFolder)
                    .conflate()
                    .transform {
                        emit(it)
                        delay(500)
                    }
                    .collect { folders ->
                        withContext(Dispatchers.Main) {
                            updatePeekOfStack {
                                currentFolderHierarchyLevel.copy(folders = folders)
                            }
                        }
                    }
            }

            val sharedFilesFlow = vault.findCipherFileEntitiesForFolder(currentFolder)
                .conflate()
                .transform {
                    emit(it)
                    delay(500)
                }
                .shareIn(this, SharingStarted.Eagerly, replay = 1)

            launch {
                sharedFilesFlow.collect { files ->
                    withContext(Dispatchers.Main) {
                        updatePeekOfStack {
                            currentFolderHierarchyLevel.copy(files = files)
                        }
                    }
                }
            }

            launch {
                sharedFilesFlow.collect { files ->
                    if (files.isEmpty()) {
                        return@collect
                    }

                    val physicalFilesById = vault.fileSystem.listFilesInCipherDirectory(currentFolder.id)
                        .toList()
                        .associateBy { Uuid.parseHex(it.fullName) }

                    withContext(Dispatchers.Main) {
                        updatePeekOfStack {
                            currentFolderHierarchyLevel.copy(physicalFilesById = physicalFilesById)
                        }
                    }
                }
            }
        }
    }

    public fun checkForVaultNameUpdates() {
        if (isRootLevel && currentFolderHierarchyLevel.folder.displayName != vault.name) {
            updatePeekOfStack {
                currentFolderHierarchyLevel.copy(folder = vault.rootFolder)
            }
        }
    }

    private fun updatePeekOfStack(updatePeek: () -> FolderHierarchyLevel) {
        synchronized(backstack) {
            backstack[backstack.lastIndex] = updatePeek()
            // No need to update the job, because the parent folder id is the same.
        }
    }

    public suspend fun createCipherFolderEntity(displayName: String): Boolean {
        try {
            vault.createFolder(displayName, currentFolderHierarchyLevel.folder)
            return true
        } catch (e: Exception) {
            logcat(LogPriority.ERROR) { e.asLog() }
            return false
        }
    }

    public suspend fun renameCipherFolderEntity(newDisplayName: String): Boolean {
        try {
            vault.renameFolder(currentFolderHierarchyLevel.folder, newDisplayName)
        } catch (e: Exception) {
            logcat(LogPriority.ERROR) { e.asLog() }
            return false
        }

        updatePeekOfStack {
            currentFolderHierarchyLevel.copy(
                folder = vault.findCipherFolderEntity(currentFolderHierarchyLevel.folder.id)
            )
        }
        return true
    }

    public fun encryptFiles(uris: List<Uri>, deleteSourceFiles: Boolean) {
        vault.scheduleFileEncryption(
            properties = WorkScheduler.NotificationProperties(R.string.encrypting_files),
            destination = currentFolderHierarchyLevel.folder,
            sources = uris.reversed().map { { fileSystem.fileInfo(it) } },
            deleteSources = deleteSourceFiles
        )
    }

    @OptIn(DelicateCoroutinesApi::class)
    public fun encryptDirectory(uri: Uri, deleteSourceFiles: Boolean) {
        GlobalScope.launch {
            vault.scheduleDirectoryEncryption(
                properties = WorkScheduler.NotificationProperties(R.string.encrypting_files),
                destination = currentFolderHierarchyLevel.folder,
                source = fileSystem.directoryInfo(uri),
                deleteSources = deleteSourceFiles
            )
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    public fun decryptSelectedCipherEntities() {
        GlobalScope.launch {
            vault.scheduleDecryption(
                properties = WorkScheduler.NotificationProperties(
                    notificationTitle = R.string.decrypting_files,
                    notificationFinishTitle = R.string.decrypted_files,
                    notificationActionText = R.string.open_directory,
                    notificationAction = Intent(Intent.ACTION_VIEW, vault.fileSystem.decryptionRootDirectory.uri as Uri)
                ),
                files = selectionState.selectedFiles,
                folders = selectionState.selectedFolders
            )

            selectionState.disableSelectionMode()
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    public fun deleteSelectedCipherEntities() {
        GlobalScope.launch {
            vault.scheduleDeletion(
                properties = WorkScheduler.NotificationProperties(R.string.deleting_files),
                files = selectionState.selectedFiles,
                folders = selectionState.selectedFolders
            )

            selectionState.disableSelectionMode()
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    public fun relocateSelectedCipherEntities() {
        GlobalScope.launch {
            vault.scheduleRelocation(
                properties = WorkScheduler.NotificationProperties(R.string.relocating_files),
                destination = currentFolderHierarchyLevel.folder,
                files = selectionState.selectedFiles,
                folders = selectionState.selectedFolders
            )

            selectionState.disableSelectionMode()
        }
    }

    public fun updateIsListLayout(isListLayout: Boolean) {
        viewModelScope.launch {
            preferences.saveIsListLayout(isListLayout)
        }
    }

    @Immutable
    public data class FolderHierarchyLevel(
        val folder: CipherFolderEntity,
        val folders: List<CipherFolderEntity> = emptyList(),
        val files: List<CipherFileEntity> = emptyList(),
        val physicalFilesById: Map<Uuid, FileInfo> = emptyMap()
    ) {
        val entitySize: Int = folders.size + files.size
    }
}
