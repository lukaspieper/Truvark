/*
 * SPDX-FileCopyrightText: 2022 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.ui.views.presenter

import android.net.Uri
import androidx.annotation.OptIn
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.ResolvingDataSource
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import coil3.ImageLoader
import de.lukaspieper.truvark.data.io.AndroidFileSystem
import de.lukaspieper.truvark.data.io.FileInfo
import de.lukaspieper.truvark.data.preferences.PersistentPreferences
import de.lukaspieper.truvark.domain.crypto.decryption.media3.DecryptingDataSource
import de.lukaspieper.truvark.domain.crypto.decryption.telephoto.CipherZoomableImageSource
import de.lukaspieper.truvark.domain.entities.CipherFileEntity
import de.lukaspieper.truvark.domain.vault.Vault
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import kotlin.uuid.Uuid

@OptIn(UnstableApi::class)
public class PresenterViewModel(
    private val preferences: PersistentPreferences,
    private val vault: Vault,
    private val fileSystem: AndroidFileSystem,
    private val imageLoader: ImageLoader,
    @InjectedParam private val folderId: Uuid,
) : ViewModel() {

    private val mediaSourceFactory by lazy {
        val decryptingDataSourceFactory = DecryptingDataSource.Factory(vault)

        val resolvingDataSourceFactory = ResolvingDataSource.Factory(decryptingDataSourceFactory) { dataSpec ->
            val file = itemsData.value.physicalFilesByUri[dataSpec.uri]
            return@Factory dataSpec.buildUpon().setCustomData(file).build()
        }

        ProgressiveMediaSource.Factory(resolvingDataSourceFactory)
    }

    public val itemsData: MutableStateFlow<ItemsData> = MutableStateFlow(ItemsData())

    public val imagesFitScreen: Flow<Boolean> = preferences.imagesFitScreen

    init {
        // TODO: Should the flow be collected here? How to update the data without interrupting the user?
        viewModelScope.launch(Dispatchers.IO) {
            val folder = vault.findCipherFolderEntity(folderId)
            val cipherFileEntities = vault.findCipherFileEntitiesForFolder(folder).first()
            itemsData.update { it.copy(cipherFileEntities = cipherFileEntities) }
        }

        viewModelScope.launch(Dispatchers.IO) {
            val physicalFiles = vault.fileSystem.listFilesInCipherDirectory(folderId).toList()

            itemsData.update { data ->
                data.copy(
                    physicalFilesById = physicalFiles.associateBy { Uuid.parseHex(it.fullName) },
                    physicalFilesByUri = physicalFiles.associateBy { it.uri }
                )
            }
        }
    }

    internal fun createCipherZoomableImageSource(fileInfo: FileInfo, mimeType: String): CipherZoomableImageSource {
        return CipherZoomableImageSource(fileInfo, imageLoader, mimeType, vault)
    }

    internal fun createMediaSource(fileInfo: FileInfo): MediaSource {
        val mediaItem = MediaItem.fromUri(fileInfo.uri as Uri)
        return mediaSourceFactory.createMediaSource(mediaItem)
    }

    @Immutable
    public data class ItemsData(
        val cipherFileEntities: List<CipherFileEntity> = emptyList(),
        val physicalFilesById: Map<Uuid, FileInfo>? = null,
        val physicalFilesByUri: Map<Any, FileInfo> = emptyMap()
    )
}
