/*
 * SPDX-FileCopyrightText: 2023 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.domain.crypto.decryption.coil

import android.net.Uri
import coil3.ImageLoader
import coil3.decode.DataSource
import coil3.decode.ImageSource
import coil3.fetch.FetchResult
import coil3.fetch.Fetcher
import coil3.fetch.SourceFetchResult
import coil3.request.Options
import de.lukaspieper.truvark.data.io.FileInfo
import de.lukaspieper.truvark.domain.crypto.decryption.DecryptingFileHandle
import de.lukaspieper.truvark.domain.vault.Vault
import okio.buffer

public class CipherFileFetcher(
    private val fileInfo: FileInfo,
    private val options: Options,
    private val vault: Vault,
) : Fetcher {

    override suspend fun fetch(): FetchResult {
        // TODO: Make this FileSystem-agnostic, e.g. by splitting file access and decryption and adding a method
        //  returning a FileHandle
        require(fileInfo.uri is Uri)

        val decryptingFileHandle = DecryptingFileHandle(options.context.contentResolver, vault, fileInfo.uri as Uri)
        val imageSource = ImageSource(decryptingFileHandle.singleSource().buffer(), options.fileSystem)

        return SourceFetchResult(
            source = imageSource,
            mimeType = decryptingFileHandle.header.mimeType,
            dataSource = DataSource.DISK
        )
    }

    public class Factory(private val vault: Vault) : Fetcher.Factory<FileInfo> {

        override fun create(data: FileInfo, options: Options, imageLoader: ImageLoader): Fetcher {
            return CipherFileFetcher(data, options, vault)
        }
    }
}
