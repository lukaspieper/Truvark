/*
 * SPDX-FileCopyrightText: 2023 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.work

import com.google.crypto.tink.StreamingAead
import de.lukaspieper.truvark.data.io.FileInfo
import de.lukaspieper.truvark.domain.IndexHandler
import de.lukaspieper.truvark.domain.entities.CipherFileEntity
import de.lukaspieper.truvark.domain.entities.CipherFolderEntity
import de.lukaspieper.truvark.domain.vault.Vault
import java.nio.ByteBuffer
import java.security.DigestInputStream
import java.security.MessageDigest
import java.util.Locale
import kotlin.time.Instant
import kotlin.uuid.Uuid

internal class EncryptingWorkBundle(
    override val properties: Properties,
    private val streamingAead: StreamingAead,
    private val vault: Vault,
    private val sources: List<Pair<() -> FileInfo, Instant>>,
    private val destination: CipherFolderEntity,
    private val destinationIndexHandler: IndexHandler<CipherFileEntity>,
    private val deleteSources: Boolean,
) : WorkBundle() {
    override val size: Int = sources.size

    override suspend fun processUnitAtIndex(index: Int) {
        val (findSource, creationInstant) = sources[index]
        val source = findSource()

        encryptFile(source, destination, creationInstant)

        if (deleteSources) {
            vault.fileSystem.delete(source)
        }
    }

    private suspend fun encryptFile(
        source: FileInfo,
        destinationFolder: CipherFolderEntity,
        creationInstant: Instant
    ): CipherFileEntity {
        val destinationFile = vault.fileSystem.createFileInCipherDirectory(destinationFolder.id, Uuid.random())

        val cipherFileEntity = CipherFileEntity(
            id = Uuid.parseHex(destinationFile.fullName),
            fullName = source.fullName,
            mimeType = source.mimeType.lowercase(Locale.getDefault()),
            fileSize = source.size,
            mediaDuration = source.mediaDuration,
            creationTimestamp = creationInstant,
            folderId = destinationFolder.id
        )

        val digest = writeFile(source, destinationFile, cipherFileEntity)
        cipherFileEntity.sha256Digest = digest

        destinationIndexHandler.addItems(cipherFileEntity)

        return cipherFileEntity
    }

    private fun writeFile(
        sourceFile: FileInfo,
        destinationFile: FileInfo,
        cipherFileEntity: CipherFileEntity
    ): ByteArray {
        val messageDigest = MessageDigest.getInstance("SHA-256")
        val metadataBytes = cipherFileEntity.toOriginalFileMetadata().toByteArray()

        streamingAead.newEncryptingStream(
            vault.fileSystem.openOutputStream(destinationFile),
            vault.id.toByteArray() + cipherFileEntity.id.toByteArray()
        ).use { encryptingOutputStream ->
            DigestInputStream(
                vault.fileSystem.openInputStream(sourceFile),
                messageDigest
            ).use { inputStream ->
                val metadataSizeAsBytes = ByteBuffer.allocate(Int.SIZE_BYTES).putInt(metadataBytes.size).array()
                encryptingOutputStream.write(metadataSizeAsBytes)

                encryptingOutputStream.write(metadataBytes)

                inputStream.copyTo(encryptingOutputStream)
            }
        }

        return messageDigest.digest()
    }
}
