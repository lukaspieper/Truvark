/*
 * SPDX-FileCopyrightText: 2022 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.ui.views.presenter

import android.content.Context
import android.media.MediaDataSource
import android.net.Uri
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil3.ImageLoader
import coil3.request.CachePolicy
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import de.lukaspieper.truvark.data.io.FileInfo
import de.lukaspieper.truvark.data.preferences.PersistentPreferences
import de.lukaspieper.truvark.domain.crypto.decryption.DecryptingFileHandle
import de.lukaspieper.truvark.domain.crypto.decryption.FileHandleMediaDataSource
import de.lukaspieper.truvark.domain.crypto.decryption.coil.CipherFileFetcher
import de.lukaspieper.truvark.domain.crypto.decryption.coil.CipherZoomableImageSource
import de.lukaspieper.truvark.domain.entities.CipherFileEntity
import de.lukaspieper.truvark.domain.vault.Vault
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = PresenterViewModel.Factory::class)
public class PresenterViewModel @AssistedInject constructor(
    private val preferences: PersistentPreferences,
    private val vault: Vault,
    @ApplicationContext private val appContext: Context,
    @Assisted private val folderId: String,
) : ViewModel() {

    private val imageLoader by lazy {
        ImageLoader.Builder(appContext)
            .components {
                add(CipherFileFetcher.Factory(vault))
            }
            .diskCachePolicy(CachePolicy.DISABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .build()
    }

    public val itemsData: MutableStateFlow<ItemsData> = MutableStateFlow(ItemsData())

    public val imagesFitScreen: Flow<Boolean> = preferences.imagesFitScreen

    init {
        // TODO: Should the flow be collected here? How to update the data without interrupting the user?
        viewModelScope.launch(Dispatchers.IO) {
            val cipherFileEntities = vault.findCipherFileEntitiesForFolder(folderId).first()
            itemsData.update { it.copy(cipherFileEntities = cipherFileEntities) }
        }

        viewModelScope.launch(Dispatchers.IO) {
            val physicalFilesById = vault.fileSystem.fetchFilesFromCipherDirectory(folderId)
                .associateBy { it.fullName }

            itemsData.update { it.copy(physicalFiles = physicalFilesById) }
        }
    }

    internal fun createCipherZoomableImageSource(fileInfo: FileInfo, mimeType: String): CipherZoomableImageSource {
        return CipherZoomableImageSource(fileInfo, imageLoader, mimeType, vault, appContext.contentResolver)
    }

    internal fun createMediaDataSource(fileInfo: FileInfo): MediaDataSource {
        return FileHandleMediaDataSource(
            // TODO: Make this FileSystem-agnostic, e.g. by splitting file access and decryption and adding a method
            //  returning a FileHandle
            DecryptingFileHandle(appContext.contentResolver, vault, fileInfo.uri as Uri)
        )
    }

    @Immutable
    public data class ItemsData(
        val cipherFileEntities: List<CipherFileEntity> = emptyList(),
        val physicalFiles: Map<String, FileInfo>? = null
    )

    @AssistedFactory
    public interface Factory {
        public fun create(folderId: String): PresenterViewModel
    }
}
