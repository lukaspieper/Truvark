/*
 * SPDX-FileCopyrightText: 2023 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.crypto

import com.google.crypto.tink.StreamingAead
import de.lukaspieper.truvark.data.io.FileInfo
import de.lukaspieper.truvark.domain.entities.OriginalFileMetadata
import de.lukaspieper.truvark.domain.vault.VaultFileSystem
import okio.FileHandle
import java.nio.ByteBuffer
import java.nio.channels.SeekableByteChannel

/**
 * A [FileHandle] that decrypts the content of a file. The header is read and decrypted during initialization and
 * it's content is available via [metadata].
 *
 * To support random file access, this class utilizes [SeekableByteChannel] that requires Android SDK 24 (N).
 */
public class DecryptingFileHandle internal constructor(
    fileSystem: VaultFileSystem,
    streamingAead: StreamingAead,
    file: FileInfo,
    associatedData: ByteArray
) : FileHandle(false) {

    private var inputStream = fileSystem.openInputStream(file)
    private var decryptingChannel = streamingAead.newSeekableDecryptingChannel(inputStream.channel, associatedData)

    private val metadataSize: Int by lazy {
        val metadataSizeBuffer = ByteBuffer.allocate(Int.SIZE_BYTES)
        val readBytes = decryptingChannel.read(metadataSizeBuffer)

        check(readBytes == Int.SIZE_BYTES) { "Could not read metadata size from encrypted file" }

        metadataSizeBuffer.rewind()
        metadataSizeBuffer.getInt()
    }

    public val metadata: OriginalFileMetadata by lazy {
        val metadataBuffer = ByteBuffer.allocate(metadataSize)
        val readBytes = decryptingChannel.read(metadataBuffer)

        check(readBytes == metadataSize) { "Could not read metadata from encrypted file" }

        OriginalFileMetadata.fromByteArray(metadataBuffer.array())
    }

    @Synchronized
    override fun protectedRead(fileOffset: Long, array: ByteArray, arrayOffset: Int, byteCount: Int): Int {
        if (byteCount == 0) return 0
        if (fileOffset >= protectedSize()) return -1

        val sizeToRead = minOf(byteCount, (protectedSize() - fileOffset).toInt())
        decryptingChannel.position(fileOffset + metadataSize + Int.SIZE_BYTES)

        val destination = ByteBuffer.wrap(array, arrayOffset, sizeToRead)
        return decryptingChannel.read(destination)
    }

    override fun protectedSize(): Long {
        return metadata.fileSize
    }

    override fun protectedWrite(fileOffset: Long, array: ByteArray, arrayOffset: Int, byteCount: Int) {
        throw NotImplementedError("This file handle is read-only.")
    }

    override fun protectedFlush() {
        throw NotImplementedError("This file handle is read-only.")
    }

    override fun protectedResize(size: Long) {
        throw NotImplementedError("This file handle is read-only.")
    }

    @Synchronized
    override fun protectedClose() {
        decryptingChannel.close()
        inputStream.close()
    }
}
