/*
 * SPDX-FileCopyrightText: 2024 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.ui.views.browser

import android.media.MediaMetadataRetriever
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemInfo
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import androidx.compose.ui.unit.toIntRect
import coil3.ImageLoader
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.SubcomposeAsyncImage
import coil3.compose.useExistingImageAsPlaceholder
import coil3.request.ImageRequest
import coil3.request.allowConversionToBitmap
import coil3.video.preferVideoFrameEmbeddedThumbnailKey
import coil3.video.videoFrameOption
import coil3.video.videoFramePercent
import de.lukaspieper.truvark.data.io.FileInfo
import de.lukaspieper.truvark.domain.crypto.decryption.coil.ThumbnailCacheInterceptor
import de.lukaspieper.truvark.domain.crypto.decryption.coil.ThumbnailCacheInterceptor.Companion.useThumbnailCache
import de.lukaspieper.truvark.domain.entities.CipherFileEntity
import de.lukaspieper.truvark.domain.entities.CipherFolderEntity
import de.lukaspieper.truvark.ui.controls.SingleLineText
import de.lukaspieper.truvark.ui.preview.BooleanPreviewParameterProvider
import de.lukaspieper.truvark.ui.preview.ElementPreviews
import de.lukaspieper.truvark.ui.preview.PreviewHost
import de.lukaspieper.truvark.ui.preview.PreviewSampleData
import de.lukaspieper.truvark.ui.theme.paddings
import de.lukaspieper.truvark.ui.views.browser.SelectionState.SelectionMode
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.roundToLong
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit
import kotlin.uuid.Uuid

@OptIn(ExperimentalCoilApi::class)
@Composable
public fun CipherEntityGrid(
    folderHierarchyLevel: BrowserViewModel.FolderHierarchyLevel,
    selectionState: SelectionState,
    isListLayout: Boolean,
    onFileClick: (CipherFileEntity) -> Unit,
    onFolderClick: (Uuid, CipherFolderEntity) -> Unit,
    contentPadding: PaddingValues,
    imageLoader: ImageLoader,
    modifier: Modifier = Modifier
) {
    val gridState = rememberLazyGridState()
    val gridCells = remember(isListLayout) {
        if (isListLayout) GridCells.Fixed(1) else GridCells.Adaptive(minSize = 150.dp)
    }

    val context = LocalContext.current

    var isDragging by remember { mutableStateOf(false) }
    val autoScrollThreshold = with(LocalDensity.current) { 40.dp.toPx() }
    var autoScrollSpeed by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(autoScrollSpeed) {
        if (autoScrollSpeed != 0f) {
            while (isActive) {
                gridState.scrollBy(autoScrollSpeed)
                delay(10)
            }
        }
    }

    // The cipherEntityGridDragHandler only considers vertical padding. Applying horizontal padding separately.
    val verticalContentPadding = PaddingValues(
        top = contentPadding.calculateTopPadding(),
        bottom = contentPadding.calculateBottomPadding()
    )
    val layoutDirection = LocalLayoutDirection.current
    val horizontalPaddingModifier = Modifier.padding(
        start = contentPadding.calculateStartPadding(layoutDirection),
        end = contentPadding.calculateEndPadding(layoutDirection)
    )

    LazyVerticalGrid(
        state = gridState,
        columns = gridCells,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.paddings.small),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.paddings.small),
        contentPadding = verticalContentPadding,
        modifier = modifier
            .fillMaxSize()
            .then(horizontalPaddingModifier)
            .cipherEntityGridDragHandler(
                lazyGridState = gridState,
                folderHierarchyLevel = folderHierarchyLevel,
                selectionState = selectionState,
                autoScrollThreshold = autoScrollThreshold,
                updateAutoScrollSpeed = { autoScrollSpeed = it },
                updateIsDragging = { isDragging = it }
            )
    ) {
        items(folderHierarchyLevel.folders, key = { it.id }) { cipherFolderEntity ->
            val isSelected by remember(selectionState.selectedFolders) {
                derivedStateOf { selectionState.selectedFolders.contains(cipherFolderEntity) }
            }

            // cipherFolderEntity may got updated (e.g. renamed)
            val clickableModifier = remember(selectionState.mode, isDragging, isSelected, cipherFolderEntity) {
                if (isDragging) return@remember Modifier
                // User should not be able to select subfolder of selected folder as destination.
                if (isSelected && selectionState.mode == SelectionMode.RELOCATION) return@remember Modifier

                Modifier.clickable {
                    when (selectionState.mode) {
                        SelectionMode.SELECTION -> selectionState.switchFolderSelection(cipherFolderEntity)
                        else -> onFolderClick(folderHierarchyLevel.folder.id, cipherFolderEntity)
                    }
                }
            }

            Card(
                modifier = Modifier
                    .requiredHeight(56.dp)
                    .animateItem()
                    .clip(CardDefaults.shape)
                    .then(clickableModifier)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.paddings.small),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.aspectRatio(1F)
                    ) {
                        Surface(
                            shape = CardDefaults.shape,
                            color = if (isSelected) MaterialTheme.colorScheme.secondary else Color.Transparent,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(MaterialTheme.paddings.extraSmall)
                        ) {
                            Icon(
                                if (isSelected) Icons.Default.CheckCircle else Icons.Default.Folder,
                                contentDescription = null,
                                modifier = Modifier.requiredSize(28.dp)
                            )
                        }
                    }

                    SingleLineText(text = cipherFolderEntity.displayName)
                }
            }
        }

        item(span = { GridItemSpan(currentLineSpan = maxCurrentLineSpan) }) {
        }

        items(folderHierarchyLevel.files, key = { it.id }) { cipherFileEntity ->
            val isSelected by remember(selectionState.selectedFiles) {
                derivedStateOf { selectionState.selectedFiles.contains(cipherFileEntity) }
            }

            val clickableModifier = remember(selectionState.mode, isDragging) {
                if (isDragging) return@remember Modifier

                when (selectionState.mode) {
                    SelectionMode.NONE -> Modifier.clickable { onFileClick(cipherFileEntity) }
                    SelectionMode.SELECTION -> Modifier.clickable {
                        selectionState.switchFileSelection(cipherFileEntity)
                    }

                    SelectionMode.RELOCATION -> Modifier
                }
            }

            val thumbnail = remember(
                cipherFileEntity.id,
                folderHierarchyLevel.physicalFilesById[cipherFileEntity.id]
            ) {
                val imageData = folderHierarchyLevel.physicalFilesById.getOrElse(cipherFileEntity.id) {
                    // When physicalFilesById is not populated yet, Coil would get a null request and won't check its
                    // cache. To give the cache a chance to have a hit, creating a fake FileInfo from cipherFileEntity.
                    FileInfo(
                        ThumbnailCacheInterceptor.ThumbnailCacheUri,
                        cipherFileEntity.id.toHexString(),
                        cipherFileEntity.fileSize,
                        "application/octet-stream"
                    )
                }

                ImageRequest.Builder(context)
                    .data(imageData)
                    .allowConversionToBitmap(true)
                    .useThumbnailCache(true)
                    .videoFramePercent(0.20) // Arbitrary value, but not too early to avoid black frames.
                    .videoFrameOption(MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                    .preferVideoFrameEmbeddedThumbnailKey(true)
                    .useExistingImageAsPlaceholder(true)
                    .build()
            }

            Card(
                modifier = Modifier
                    .animateItem()
                    .clip(CardDefaults.shape)
                    .then(clickableModifier)
                    .then(if (isListLayout) Modifier else Modifier.aspectRatio(1F))
            ) {
                if (isListLayout) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.paddings.small),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        var isError by remember { mutableStateOf(false) }

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.requiredSize(56.dp)
                        ) {
                            SubcomposeAsyncImage(
                                model = thumbnail,
                                imageLoader = imageLoader,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                onError = { isError = true }
                            )

                            if (isError || isSelected) {
                                Surface(
                                    shape = CardDefaults.shape,
                                    color = if (isSelected) MaterialTheme.colorScheme.secondary else Color.Transparent,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(MaterialTheme.paddings.extraSmall)
                                ) {
                                    Icon(
                                        if (isSelected) Icons.Default.CheckCircle else Icons.AutoMirrored.Default.InsertDriveFile,
                                        contentDescription = null,
                                        modifier = Modifier.requiredSize(28.dp)
                                    )
                                }
                            }
                        }

                        Column {
                            SingleLineText(text = cipherFileEntity.fullName)

                            Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.paddings.large)) {
                                SingleLineText(
                                    text = cipherFileEntity.mimeType,
                                    fontStyle = FontStyle.Italic,
                                    fontSize = MaterialTheme.typography.bodyMedium.fontSize
                                )

                                cipherFileEntity.mediaDuration?.let { mediaDuration ->
                                    SingleLineText(
                                        text = mediaDuration.roundToSeconds().toString(),
                                        fontStyle = FontStyle.Italic,
                                        fontSize = MaterialTheme.typography.bodyMedium.fontSize
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Box {
                        SubcomposeAsyncImage(
                            model = thumbnail,
                            imageLoader = imageLoader,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                            error = {
                                Column(
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(MaterialTheme.paddings.large)
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Default.InsertDriveFile,
                                        contentDescription = null,
                                        modifier = Modifier.defaultMinSize(64.dp, 64.dp)
                                    )
                                    Spacer(modifier = Modifier.size(MaterialTheme.paddings.medium))

                                    Text(
                                        text = cipherFileEntity.fullName,
                                        maxLines = 3,
                                        overflow = TextOverflow.Ellipsis,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        )

                        if (isSelected) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.requiredSize(56.dp)
                            ) {
                                Surface(
                                    shape = CardDefaults.shape,
                                    color = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(MaterialTheme.paddings.extraSmall)
                                ) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        modifier = Modifier.requiredSize(28.dp)
                                    )
                                }
                            }
                        }

                        cipherFileEntity.mediaDuration?.let { mediaDuration ->
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                                ),
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(
                                        bottom = MaterialTheme.paddings.extraSmall,
                                        end = MaterialTheme.paddings.extraSmall
                                    )
                            ) {
                                Text(
                                    text = mediaDuration.roundToSeconds().toString(),
                                    modifier = Modifier.padding(MaterialTheme.paddings.extraSmall)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Suppress("UNCHECKED_CAST")
private fun Modifier.cipherEntityGridDragHandler(
    lazyGridState: LazyGridState,
    folderHierarchyLevel: BrowserViewModel.FolderHierarchyLevel,
    selectionState: SelectionState,
    autoScrollThreshold: Float,
    updateAutoScrollSpeed: (Float) -> Unit,
    updateIsDragging: (Boolean) -> Unit
): Modifier {
    if (selectionState.mode == SelectionMode.RELOCATION) return this

    return this.pointerInput(folderHierarchyLevel, selectionState) {
        fun LazyGridState.gridItemKeyAtPosition(hitPoint: Offset): LazyGridItemInfo? {
            return layoutInfo.visibleItemsInfo.find { itemInfo ->
                val paddingAwareHitPoint = hitPoint.copy(y = hitPoint.y + layoutInfo.viewportStartOffset)
                itemInfo.size.toIntRect().contains(paddingAwareHitPoint.round() - itemInfo.offset)
            }
        }

        val allGridEntities = folderHierarchyLevel.folders
            .plus(List(lazyGridState.layoutInfo.totalItemsCount - folderHierarchyLevel.entitySize) { null })
            .plus(folderHierarchyLevel.files)

        var initial: LazyGridItemInfo? = null
        var previous: LazyGridItemInfo? = null

        detectDragGesturesAfterLongPress(
            onDragStart = { offset ->
                lazyGridState.gridItemKeyAtPosition(offset)?.let { current ->
                    initial = current
                    previous = current
                    updateIsDragging(true)

                    when (val entity = allGridEntities.getOrNull(current.index)) {
                        is CipherFolderEntity -> selectionState.selectFolders(setOf(entity))
                        is CipherFileEntity -> selectionState.selectFiles(setOf(entity))
                    }
                }
            },
            onDragCancel = {
                initial = null
                updateAutoScrollSpeed(0f)
                updateIsDragging(false)
            },
            onDragEnd = {
                initial = null
                updateAutoScrollSpeed(0f)
                updateIsDragging(false)
            },
            onDrag = { change, _ ->
                if (initial != null) {
                    val distFromBottom = lazyGridState.layoutInfo.viewportSize.height - change.position.y
                    val distFromTop = change.position.y
                    val newAutoScrollSpeed = when {
                        distFromBottom < autoScrollThreshold -> autoScrollThreshold - distFromBottom
                        distFromTop < autoScrollThreshold -> -(autoScrollThreshold - distFromTop)
                        else -> 0f
                    }
                    updateAutoScrollSpeed(newAutoScrollSpeed)

                    lazyGridState.gridItemKeyAtPosition(change.position)?.let { current ->
                        if (previous != current) {
                            val currentIndices = when {
                                initial!!.index < current.index -> initial!!.index..current.index
                                else -> current.index..initial!!.index
                            }
                            val previousIndices = when {
                                initial!!.index < previous!!.index -> initial!!.index..previous!!.index
                                else -> previous!!.index..initial!!.index
                            }

                            val entitiesToSelect = (currentIndices - previousIndices)
                                .mapNotNull { allGridEntities.getOrNull(it) }
                                .groupBy { entity -> entity::class }

                            selectionState.selectFolders(
                                entitiesToSelect[CipherFolderEntity::class].orEmpty() as List<CipherFolderEntity>
                            )
                            selectionState.selectFiles(
                                entitiesToSelect[CipherFileEntity::class].orEmpty() as List<CipherFileEntity>
                            )

                            val entitiesToDeselect = (previousIndices - currentIndices)
                                .mapNotNull { allGridEntities.getOrNull(it) }
                                .groupBy { entity -> entity::class }

                            selectionState.deselectFolders(
                                entitiesToDeselect[CipherFolderEntity::class].orEmpty() as List<CipherFolderEntity>
                            )
                            selectionState.deselectFiles(
                                entitiesToDeselect[CipherFileEntity::class].orEmpty() as List<CipherFileEntity>
                            )

                            previous = current
                        }
                    }
                }
            }
        )
    }
}

private fun Duration.roundToSeconds(): Duration {
    return toDouble(DurationUnit.SECONDS).roundToLong().seconds
}

@ElementPreviews
@Composable
private fun CipherEntityGridSelectionPreviews(
    @PreviewParameter(BooleanPreviewParameterProvider::class) isListLayout: Boolean
) = PreviewHost {
    with(PreviewSampleData) {
        CipherEntityGrid(
            folderHierarchyLevel = folderHierarchyLevel,
            selectionState = SelectionState(
                initialSelectedFolders = setOf(cipherFolderEntities[2], cipherFolderEntities[4]),
                initialSelectedFiles = setOf(cipherFileEntities[2], cipherFileEntities[5], cipherFileEntities[6]),
            ),
            isListLayout = isListLayout,
            onFileClick = {},
            onFolderClick = { _, _ -> },
            contentPadding = PaddingValues(MaterialTheme.paddings.large),
            imageLoader = ImageLoader(LocalContext.current),
            modifier = Modifier
        )
    }
}
