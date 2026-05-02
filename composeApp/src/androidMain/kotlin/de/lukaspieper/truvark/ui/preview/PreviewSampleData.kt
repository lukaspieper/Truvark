/*
 * SPDX-FileCopyrightText: 2024 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.ui.preview

import de.lukaspieper.truvark.domain.entities.CipherFileEntity
import de.lukaspieper.truvark.domain.entities.CipherFolderEntity
import de.lukaspieper.truvark.ui.views.browser.BrowserViewModel
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
        CipherFileEntity(
            id = index.toPreviewUuid(),
            fullName = "Top Secret File $index.file",
            mimeType = "application/octet-stream",
            fileSize = 0L,
            mediaDuration = if (index % 7 == 0) 62.seconds else null,
            creationTimestamp = Clock.System.now(),
            folderId = Uuid.NIL // Usually files cannot be in the root folder.
        )
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
