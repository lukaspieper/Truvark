/*
 * SPDX-FileCopyrightText: 2026 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.domain

import com.google.crypto.tink.StreamingAead
import de.lukaspieper.truvark.domain.entities.CipherFileEntity
import de.lukaspieper.truvark.domain.entities.CipherFolderEntity
import de.lukaspieper.truvark.domain.vault.VaultConfig
import de.lukaspieper.truvark.domain.vault.VaultFileSystem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.protobuf.ProtoBuf
import logcat.LogPriority
import logcat.logcat
import java.io.InputStream
import java.io.OutputStream

/**
 * This handler provides a simple thread-safe CRUD abstraction over an encrypted index file. Index files can either
 * store a list of [CipherFolderEntity] or [CipherFileEntity] objects.
 *
 * There should be only one instance of an [IndexHandler] per index file to avoid concurrent writes and to enable
 * observation of changes via [items].
 */
internal abstract class IndexHandler<T>(
    private val streamingAead: StreamingAead,
    private val associatedData: ByteArray,
    private val serializer: KSerializer<T>
) {
    private val mutex = Mutex()

    private val _items by lazy {
        val items = runBlocking { readIndexFileOrNull() }?.use { inputStream ->
            if (inputStream.available() == 0) return@use emptyList()

            streamingAead.newDecryptingStream(inputStream, associatedData).use { decryptingStream ->
                val bytes = decryptingStream.readBytes()
                ProtoBuf.decodeFromByteArray(ListSerializer(serializer), bytes)
            }
        } ?: emptyList()

        processPostDeserialization(items)
        MutableStateFlow<List<T>>(items)
    }

    internal val items: StateFlow<List<T>> by lazy { _items.asStateFlow() }

    protected abstract suspend fun readIndexFileOrNull(): InputStream?
    protected abstract suspend fun writeIndexFile(): OutputStream
    protected abstract fun processPostDeserialization(items: List<T>)

    internal suspend fun addItems(vararg items: T) {
        mutex.withLock {
            val updatedItems = _items.value + items
            writeItems(updatedItems)
            _items.value = updatedItems
        }
    }

    internal suspend fun updateItem(oldItem: T, newItem: T) {
        mutex.withLock {
            val updatedItems = _items.value.map { if (it == oldItem) newItem else it }
            writeItems(updatedItems)
            _items.value = updatedItems
        }
    }

    internal suspend fun deleteItems(vararg items: T) {
        mutex.withLock {
            val updatedItems = _items.value - items.toSet()
            writeItems(updatedItems)
            _items.value = updatedItems
        }
    }

    private suspend fun writeItems(items: List<T>) {
        streamingAead.newEncryptingStream(writeIndexFile(), associatedData).use { encryptingStream ->
            val bytes = ProtoBuf.encodeToByteArray(ListSerializer(serializer), items)
            encryptingStream.write(bytes)
        }

        logcat(LogPriority.DEBUG) { "Write to index file completed." }
    }

    internal class FolderIndexHandler(
        streamingAead: StreamingAead,
        private val vaultFileSystem: VaultFileSystem,
        vaultConfig: VaultConfig
    ) : IndexHandler<CipherFolderEntity>(
        streamingAead = streamingAead,
        associatedData = vaultConfig.id.toByteArray() + "FolderIndex".encodeToByteArray(),
        serializer = CipherFolderEntity.serializer()
    ) {
        override suspend fun readIndexFileOrNull(): InputStream {
            return vaultFileSystem.openInputStream(vaultFileSystem.folderIndexFile)
        }

        override suspend fun writeIndexFile(): OutputStream {
            return vaultFileSystem.openOutputStream(vaultFileSystem.folderIndexFile)
        }

        override fun processPostDeserialization(items: List<CipherFolderEntity>) {
            // No additional processing needed.
        }
    }

    internal class FileIndexHandler(
        streamingAead: StreamingAead,
        private val vaultFileSystem: VaultFileSystem,
        vaultConfig: VaultConfig,
        private val folder: CipherFolderEntity
    ) : IndexHandler<CipherFileEntity>(
        streamingAead = streamingAead,
        associatedData = vaultConfig.id.toByteArray() + folder.id.toByteArray(),
        serializer = CipherFileEntity.serializer()
    ) {
        override suspend fun readIndexFileOrNull(): InputStream? {
            val indexFile = vaultFileSystem.findFileIndexFileOrNull(folder.id)
            return indexFile?.let { vaultFileSystem.openInputStream(it) }
        }

        override suspend fun writeIndexFile(): OutputStream {
            val indexFile = vaultFileSystem.findOrCreateFileIndexFile(folder.id)
            return vaultFileSystem.openOutputStream(indexFile)
        }

        override fun processPostDeserialization(items: List<CipherFileEntity>) {
            items.forEach { file -> file.folderId = folder.id }
        }
    }
}
