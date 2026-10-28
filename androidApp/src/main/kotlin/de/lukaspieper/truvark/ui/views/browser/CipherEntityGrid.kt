/*
 * SPDX-FileCopyrightText: 2024 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.ui.views.browser

import android.content.ContentResolver
import android.media.MediaMetadataRetriever
import android.text.format.Formatter
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemInfo
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
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
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Dp
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
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import de.lukaspieper.truvark.data.io.FileInfo
import de.lukaspieper.truvark.domain.crypto.decryption.coil.ThumbnailCacheInterceptor
import de.lukaspieper.truvark.domain.crypto.decryption.coil.ThumbnailCacheInterceptor.Companion.useThumbnailCache
import de.lukaspieper.truvark.domain.entities.CipherFileEntity
import de.lukaspieper.truvark.domain.entities.CipherFolderEntity
import de.lukaspieper.truvark.ui.controls.ShapedIcon
import de.lukaspieper.truvark.ui.controls.SingleLineText
import de.lukaspieper.truvark.ui.icons.MaterialSymbols
import de.lukaspieper.truvark.ui.preview.BooleanPreviewParameterProvider
import de.lukaspieper.truvark.ui.preview.ElementPreviews
import de.lukaspieper.truvark.ui.preview.PreviewHost
import de.lukaspieper.truvark.ui.preview.PreviewSampleData
import de.lukaspieper.truvark.ui.theme.defaultListItemColors
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
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme

    val gridState = rememberLazyGridState()
    val gridCells = remember(isListLayout) {
        if (isListLayout) GridCells.Fixed(1) else GridCells.Adaptive(minSize = 150.dp)
    }

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
        verticalArrangement = Arrangement.spacedBy(
            if (isListLayout) ListItemDefaults.SegmentedGap else MaterialTheme.paddings.small
        ),
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
        itemsIndexed(folderHierarchyLevel.folders, key = { _, item -> item.id }) { index, cipherFolderEntity ->
            val isSelected by remember(selectionState.selectedFolders) {
                derivedStateOf { selectionState.selectedFolders.contains(cipherFolderEntity) }
            }

            SegmentedListItem(
                modifier = Modifier.animateItem(),
                onClick = {
                    if (!isDragging) {
                        when (selectionState.mode) {
                            SelectionMode.SELECTION -> selectionState.switchFolderSelection(cipherFolderEntity)
                            else -> onFolderClick(folderHierarchyLevel.folder.id, cipherFolderEntity)
                        }
                    }
                },
                enabled = !(isSelected && selectionState.mode == SelectionMode.RELOCATION),
                shapes = when {
                    isListLayout -> ListItemDefaults.segmentedShapes(index, folderHierarchyLevel.folders.size)
                    else -> ListItemDefaults.shapes(MaterialTheme.shapes.large)
                },
                colors = MaterialTheme.defaultListItemColors,
                leadingContent = {
                    ShapedIcon(
                        imageVector = if (isSelected) MaterialSymbols.Check else MaterialSymbols.Folder,
                        tint = if (isSelected) colorScheme.onPrimaryContainer else colorScheme.primary,
                        shape = if (isSelected) MaterialShapes.Circle else MaterialShapes.Square,
                        size = if (isListLayout) 48.dp else 40.dp,
                        background = when {
                            isSelected -> colorScheme.primaryContainer
                            else -> colorScheme.primary.copy(alpha = 0.16f)
                        }
                    )
                },
                content = { SingleLineText(text = cipherFolderEntity.displayName) }
            )
        }

        if (folderHierarchyLevel.folders.isNotEmpty() && folderHierarchyLevel.files.isNotEmpty()) {
            item(span = { GridItemSpan(currentLineSpan = maxLineSpan) }) {
                Spacer(modifier = Modifier.height(MaterialTheme.paddings.large))
            }
        }

        itemsIndexed(folderHierarchyLevel.files, key = { _, item -> item.id }) { index, cipherFileEntity ->
            val isSelected by remember(selectionState.selectedFiles) {
                derivedStateOf { selectionState.selectedFiles.contains(cipherFileEntity) }
            }
            val configuration = LocalConfiguration.current
            val mimeTypeInfo = remember(context, configuration, cipherFileEntity.mimeType) {
                context.contentResolver.getTypeInfo(cipherFileEntity.mimeType)
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

            if (isListLayout) {
                SegmentedListItem(
                    modifier = Modifier
                        .animateItem()
                        .then(clickableModifier),
                    enabled = !(isSelected && selectionState.mode == SelectionMode.RELOCATION),
                    shapes = ListItemDefaults.segmentedShapes(index, folderHierarchyLevel.files.size),
                    colors = MaterialTheme.defaultListItemColors,
                    leadingContent = {
                        when {
                            isSelected -> ShapedIcon(
                                imageVector = MaterialSymbols.Check,
                                tint = colorScheme.onPrimaryContainer,
                                shape = MaterialShapes.Circle,
                                background = colorScheme.primaryContainer
                            )

                            else -> SubcomposeAsyncImage(
                                model = thumbnail,
                                imageLoader = imageLoader,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                error = { MimeTypeIcon(mimeTypeInfo) },
                                modifier = Modifier
                                    .requiredSize(48.dp)
                                    .clip(MaterialShapes.Square.toShape())
                            )
                        }
                    },
                    content = { SingleLineText(text = cipherFileEntity.fullName) },
                    supportingContent = {
                        val supportingText = listOfNotNull(
                            mimeTypeInfo.label.toString(),
                            Formatter.formatFileSize(context, cipherFileEntity.fileSize),
                            cipherFileEntity.mediaDuration?.roundToSeconds()?.toString()
                        ).joinToString(" · ")

                        SingleLineText(
                            text = supportingText,
                            fontSize = MaterialTheme.typography.bodyMedium.fontSize
                        )
                    }
                )
            } else {
                CipherFileGridTile(
                    cipherFileEntity = cipherFileEntity,
                    mimeTypeInfo = mimeTypeInfo,
                    thumbnail = thumbnail,
                    imageLoader = imageLoader,
                    isSelected = isSelected,
                    isRelocation = selectionState.mode == SelectionMode.RELOCATION,
                    modifier = Modifier
                        .animateItem()
                        .clip(if (isSelected) MaterialTheme.shapes.extraExtraLarge else MaterialTheme.shapes.large)
                        .then(clickableModifier)
                )
            }
        }
    }
}

@Composable
private fun CipherFileGridTile(
    cipherFileEntity: CipherFileEntity,
    mimeTypeInfo: ContentResolver.MimeTypeInfo,
    thumbnail: ImageRequest,
    imageLoader: ImageLoader,
    isSelected: Boolean,
    isRelocation: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        modifier = modifier.aspectRatio(1F)
    ) {
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
                            .padding(
                                horizontal = MaterialTheme.paddings.large,
                                vertical = MaterialTheme.paddings.medium
                            )
                    ) {
                        MimeTypeIcon(mimeTypeInfo = mimeTypeInfo, size = 64.dp)
                        Spacer(modifier = Modifier.size(MaterialTheme.paddings.medium))
                        Text(
                            text = cipherFileEntity.fullName,
                            style = MaterialTheme.typography.bodyLarge,
                            minLines = 2,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            )

            cipherFileEntity.mediaDuration?.let { mediaDuration ->
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(MaterialTheme.paddings.small)
                ) {
                    Text(
                        text = mediaDuration.roundToSeconds().toString(),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(
                            horizontal = MaterialTheme.paddings.small,
                            vertical = MaterialTheme.paddings.extraSmall
                        )
                    )
                }
            }

            if (isRelocation && isSelected) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
                )
            }

            if (isSelected) {
                ShapedIcon(
                    imageVector = MaterialSymbols.Check,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    shape = MaterialShapes.Circle,
                    size = 40.dp,
                    background = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.padding(MaterialTheme.paddings.large)
                )
            }
        }
    }
}

@Composable
private fun MimeTypeIcon(
    mimeTypeInfo: ContentResolver.MimeTypeInfo,
    size: Dp = 48.dp
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val drawable = remember(context, configuration, mimeTypeInfo) {
        mimeTypeInfo.icon.loadDrawable(context)
    }

    ShapedIcon(
        painter = when {
            drawable != null -> rememberDrawablePainter(drawable)
            else -> rememberVectorPainter(MaterialSymbols.InsertDriveFile)
        },
        tint = if (drawable != null) Color.Unspecified else MaterialTheme.colorScheme.tertiary,
        shape = MaterialShapes.Square,
        size = size,
        background = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.16f)
    )
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
