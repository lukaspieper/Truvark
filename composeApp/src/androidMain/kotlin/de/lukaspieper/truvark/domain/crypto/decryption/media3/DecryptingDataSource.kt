/*
 * SPDX-FileCopyrightText: 2025 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.domain.crypto.decryption.media3

import android.net.Uri
import androidx.media3.common.PlaybackException.ERROR_CODE_IO_READ_POSITION_OUT_OF_RANGE
import androidx.media3.common.PlaybackException.ERROR_CODE_IO_UNSPECIFIED
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.BaseDataSource
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSourceException
import androidx.media3.datasource.DataSpec
import de.lukaspieper.truvark.crypto.DecryptingFileHandle
import de.lukaspieper.truvark.data.io.FileInfo
import de.lukaspieper.truvark.domain.vault.Vault
import okio.FileHandle

/**
 * A [DataSource] decrypting media files utilizing [DecryptingFileHandle].
 */
@UnstableApi
public class DecryptingDataSource private constructor(private val vault: Vault) : BaseDataSource(false) {

    private lateinit var uri: Uri
    private lateinit var fileHandle: FileHandle
    private var position = 0L

    @Throws(IllegalArgumentException::class)
    override fun open(dataSpec: DataSpec): Long {
        uri = dataSpec.uri.normalizeScheme()
        transferInitializing(dataSpec)

        val file = dataSpec.customData as? FileInfo ?: throw DataSourceException(ERROR_CODE_IO_UNSPECIFIED)
        fileHandle = vault.createDecryptingFileHandle(file)

        if (dataSpec.position !in 0..<fileHandle.size()) {
            throw DataSourceException(ERROR_CODE_IO_READ_POSITION_OUT_OF_RANGE)
        }

        position = dataSpec.position
        transferStarted(dataSpec)

        return fileHandle.size() - dataSpec.position
    }

    override fun read(buffer: ByteArray, offset: Int, readLength: Int): Int {
        val readBytes = fileHandle.read(position, buffer, offset, readLength)

        if (readBytes >= 0) {
            position += readBytes
            bytesTransferred(readBytes)
        }

        return readBytes
    }

    override fun getUri(): Uri {
        return uri
    }

    override fun close() {
        fileHandle.close()
        transferEnded()
    }

    public class Factory(private val vault: Vault) : DataSource.Factory {

        override fun createDataSource(): DecryptingDataSource {
            return DecryptingDataSource(vault)
        }
    }
}
