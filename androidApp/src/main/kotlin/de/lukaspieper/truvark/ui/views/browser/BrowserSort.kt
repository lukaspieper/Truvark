/*
 * SPDX-FileCopyrightText: 2026 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.ui.views.browser

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector
import de.lukaspieper.truvark.R
import de.lukaspieper.truvark.domain.entities.CipherFileEntity
import de.lukaspieper.truvark.domain.entities.CipherFolderEntity
import de.lukaspieper.truvark.ui.icons.MaterialSymbols

internal enum class BrowserSortCriterion(
    @StringRes val label: Int,
    val icon: ImageVector,
    val folderComparator: Comparator<CipherFolderEntity>,
    val fileComparator: Comparator<CipherFileEntity>,
) {
    NAME(
        label = R.string.browser_sort_name,
        icon = MaterialSymbols.TextFields,
        folderComparator = compareByName(CipherFolderEntity::displayName),
        fileComparator = compareByName(CipherFileEntity::fullName),
    ),
    CREATION_TIMESTAMP(
        label = R.string.browser_sort_creation_timestamp,
        icon = MaterialSymbols.CalendarToday,
        folderComparator = compareBy(CipherFolderEntity::creationTimestamp),
        fileComparator = compareBy(CipherFileEntity::creationTimestamp),
    ),
}

internal enum class BrowserSortDirection {
    ASCENDING,
    DESCENDING
}

@Immutable
internal data class BrowserSortState(
    val criterion: BrowserSortCriterion = BrowserSortCriterion.CREATION_TIMESTAMP,
    val direction: BrowserSortDirection = BrowserSortDirection.DESCENDING,
) {
    internal val folderComparator: Comparator<CipherFolderEntity>
        get() = this.criterion.folderComparator.withDirection(direction)

    internal val fileComparator: Comparator<CipherFileEntity>
        get() = this.criterion.fileComparator.withDirection(direction)
}

/** Compares names case-insensitively first, then case-sensitively for equal spellings. */
private fun <T> compareByName(name: (T) -> String): Comparator<T> {
    return compareBy(String.CASE_INSENSITIVE_ORDER, name).thenBy(name)
}

private fun <T> Comparator<T>.withDirection(direction: BrowserSortDirection): Comparator<T> {
    return when (direction) {
        BrowserSortDirection.ASCENDING -> this
        BrowserSortDirection.DESCENDING -> reversed()
    }
}
