/*
 * SPDX-FileCopyrightText: 2024 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.ui.preview

import de.lukaspieper.truvark.domain.entities.CipherFileEntity
import de.lukaspieper.truvark.domain.entities.CipherFolderEntity
import de.lukaspieper.truvark.ui.views.browser.BrowserViewModel
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.Uuid

// Files and folders cannot use the same indices because the selection logic requires unique Uuids for all items.
public object PreviewSampleData {

    public fun Int.toPreviewUuid(): Uuid {
        return Uuid.fromLongs(this.toLong(), 0L)
    }

    public val cipherFolderEntities: List<CipherFolderEntity> = (100..<105).map { index ->
        CipherFolderEntity(
            id = index.toPreviewUuid(),
            displayName = "Personal Folder $index",
            parentFolderId = Uuid.NIL,
            creationTimestamp = Clock.System.now()
        )
    }

    public val cipherFileEntities: List<CipherFileEntity> = (1..<30).map { index ->
        val hasMediaDuration = index % 5 == 0
        val hasMediaDimensions = index % 3 == 0

        CipherFileEntity(
            id = index.toPreviewUuid(),
            fullName = "$index -${" secret".repeat(Random.nextInt(1, 6))}.file",
            mimeType = "application/octet-stream",
            fileSize = Random.nextLong(100, 5_000_000),
            creationTimestamp = Clock.System.now(),
            folderId = Uuid.NIL, // Usually files cannot be in the root folder.
            mediaDuration = if (hasMediaDuration) Random.nextInt(1, 900).seconds else null,
            mediaWidth = if (hasMediaDimensions) Random.nextInt(480, 1920) else null,
            mediaHeight = if (hasMediaDimensions) Random.nextInt(480, 1920) else null
        ).apply {
            sha256Digest = ByteArray(32)
        }
    }

    public val folderHierarchyLevel: BrowserViewModel.FolderHierarchyLevel = BrowserViewModel.FolderHierarchyLevel(
        folder = CipherFolderEntity(
            id = Uuid.NIL, // Root folder.
            displayName = "Vault",
            parentFolderId = Uuid.NIL,
            creationTimestamp = Clock.System.now()
        ),
        folders = cipherFolderEntities,
        files = cipherFileEntities
    )
}
