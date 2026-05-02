/*
 * SPDX-FileCopyrightText: 2024 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.test

import de.lukaspieper.truvark.domain.entities.CipherFileEntity
import de.lukaspieper.truvark.domain.entities.RealmCipherFileEntity
import de.lukaspieper.truvark.domain.entities.RealmCipherFolderEntity
import de.lukaspieper.truvark.domain.findCipherFolderEntity
import de.lukaspieper.truvark.domain.vault.internal.CipherFolderEntityCreator
import io.realm.kotlin.Realm
import io.realm.kotlin.TypedRealm
import io.realm.kotlin.ext.query
import io.realm.kotlin.query.RealmResults
import kotlin.uuid.Uuid

internal interface RealmExtensions {

    fun TypedRealm.findCipherFileEntityByName(name: String): RealmResults<RealmCipherFileEntity> {
        return query<RealmCipherFileEntity>(
            when {
                name.contains('.') -> """
                ${RealmCipherFileEntity.REALM_FIELD_ORIGINAL_NAME} == '${name.substringBeforeLast('.')}' AND
                ${RealmCipherFileEntity.REALM_FIELD_FILE_EXTENSION} == '${name.substringAfterLast('.')}'
                """.trimIndent()

                else -> "${RealmCipherFileEntity.REALM_FIELD_ORIGINAL_NAME} == '$name'"
            }
        ).find()
    }

    /**
     * Similar to [CipherFolderEntityCreator] but without error handling. For simplicity, the generated ID is also used as
     * display name.
     */
    fun Realm.createRandomCipherFolderEntity(parentFolderId: String = ""): RealmCipherFolderEntity {
        return writeBlocking {
            val parentFolderEntity = when {
                parentFolderId.isNotBlank() -> findCipherFolderEntity(parentFolderId)
                else -> null
            }

            val folderId = Uuid.random().toHexString()
            val folder = RealmCipherFolderEntity().apply {
                id = folderId
                displayName = folderId
                parentFolder = parentFolderEntity
            }

            copyToRealm(folder)
        }
    }

    fun Realm.createRandomCipherFileEntity(parentFolder: RealmCipherFolderEntity): CipherFileEntity {
        return writeBlocking {
            val cipherFileEntity = RealmCipherFileEntity().apply {
                id = Uuid.random().toHexString()
                folder = findLatest(parentFolder)!!
            }

            copyToRealm(cipherFileEntity)
        }
    }
}
