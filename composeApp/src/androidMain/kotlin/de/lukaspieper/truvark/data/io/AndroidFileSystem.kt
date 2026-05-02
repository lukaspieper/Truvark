/*
 * SPDX-FileCopyrightText: 2023 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.data.io

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.media.MediaMetadataRetriever
import android.media.MediaMetadataRetriever.METADATA_KEY_DURATION
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.DocumentsContract.Document.COLUMN_DISPLAY_NAME
import android.provider.DocumentsContract.Document.COLUMN_DOCUMENT_ID
import android.provider.DocumentsContract.Document.COLUMN_MIME_TYPE
import android.provider.DocumentsContract.Document.COLUMN_SIZE
import android.provider.DocumentsContract.Document.MIME_TYPE_DIR
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import logcat.LogPriority
import logcat.asLog
import logcat.logcat
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.io.OutputStream
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * A [FileSystem] implementation for Android's Storage Access Framework (content:// URIs).
 */
public class AndroidFileSystem(private val context: Context) : FileSystem() {

    public fun appFilesDir(): File {
        return context.filesDir
    }

    public fun takePersistableUriPermission(uri: Uri) {
        require(uri != Uri.EMPTY) { "uri must not be empty" }

        context.contentResolver.takePersistableUriPermission(
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        )
    }

    public fun fileInfo(uri: Uri): FileInfo {
        require(uri != Uri.EMPTY) { "uri must not be empty" }

        context.contentResolver.query(
            uri,
            arrayOf(
                COLUMN_DISPLAY_NAME,
                COLUMN_MIME_TYPE,
                COLUMN_SIZE
            )
        ) { cursor ->
            if (cursor?.moveToFirst() == true && cursor.getString(COLUMN_MIME_TYPE) != MIME_TYPE_DIR) {
                val mimeType = cursor.getString(COLUMN_MIME_TYPE)
                var mediaDuration: Duration? = null

                if (mimeType.startsWith("video/") || mimeType.startsWith("audio/")) {
                    val retriever = MediaMetadataRetriever()
                    try {
                        retriever.setDataSource(context, uri)
                        mediaDuration = retriever.extractMetadata(METADATA_KEY_DURATION)?.toLongOrNull()?.milliseconds
                    } catch (e: Exception) {
                        logcat(LogPriority.WARN) { e.asLog() }
                    } finally {
                        retriever.release()
                    }
                }

                return FileInfo(
                    uri = uri,
                    fullName = cursor.getString(COLUMN_DISPLAY_NAME),
                    mimeType = cursor.getString(COLUMN_MIME_TYPE),
                    size = cursor.getLong(COLUMN_SIZE),
                    mediaDuration = mediaDuration
                )
            }
        }

        throw FileNotFoundException()
    }

    @Throws(Exception::class)
    public fun directoryInfo(treeUri: Uri): DirectoryInfo {
        require(treeUri != Uri.EMPTY) { "uri must not be empty" }

        val uri = convertTreeUriToDocumentUri(treeUri)

        context.contentResolver.query(
            uri,
            arrayOf(
                COLUMN_DOCUMENT_ID,
                COLUMN_DISPLAY_NAME,
                COLUMN_MIME_TYPE
            )
        ) { cursor ->
            if (cursor?.moveToFirst() == true && cursor.getString(COLUMN_MIME_TYPE) == MIME_TYPE_DIR) {
                return DirectoryInfo(
                    uri = DocumentsContract.buildDocumentUriUsingTree(
                        uri,
                        cursor.getString(COLUMN_DOCUMENT_ID)
                    ),
                    name = cursor.getString(COLUMN_DISPLAY_NAME)
                )
            }
        }

        throw FileNotFoundException()
    }

    private fun convertTreeUriToDocumentUri(uri: Uri): Uri {
        val documentId = when {
            DocumentsContract.isDocumentUri(context, uri) -> DocumentsContract.getDocumentId(uri)
            else -> DocumentsContract.getTreeDocumentId(uri)
        }

        return DocumentsContract.buildDocumentUriUsingTree(uri, documentId)
    }

    @Throws(Exception::class)
    override suspend fun createFile(directoryInfo: DirectoryInfo, name: String, mimeType: String): FileInfo {
        val uri = directoryInfo.uri as Uri
        if (findFileOrNull(directoryInfo, name) != null) throw IOException("File already exists")

        val fileUri = DocumentsContract.createDocument(context.contentResolver, uri, mimeType, name)
            ?: throw IOException("Could not create file")

        return FileInfo(fileUri, name, 0, mimeType)
    }

    @Throws(Exception::class)
    override fun createDirectory(directoryInfo: DirectoryInfo, name: String): DirectoryInfo {
        val uri = directoryInfo.uri as Uri

        val directoryUri = DocumentsContract.createDocument(
            context.contentResolver,
            uri,
            MIME_TYPE_DIR,
            name
        ) ?: throw IOException()

        // In case a directory with that name already exists, a number is appended. The method is private and only used
        // from findOrCreateDirectory(), for now this should be safe.
        return DirectoryInfo(directoryUri, name)
    }

    override fun listFiles(directoryInfo: DirectoryInfo): Flow<FileInfo> = flow {
        val uri = directoryInfo.uri as Uri
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(uri, DocumentsContract.getDocumentId(uri))

        context.contentResolver.query(
            childrenUri,
            arrayOf(COLUMN_DOCUMENT_ID, COLUMN_DISPLAY_NAME, COLUMN_SIZE, COLUMN_MIME_TYPE),
        ) { cursor ->
            while (cursor?.moveToNext() == true) {
                val documentType = cursor.getString(COLUMN_MIME_TYPE)
                if (documentType == MIME_TYPE_DIR) continue

                emit(
                    FileInfo(
                        uri = DocumentsContract.buildDocumentUriUsingTree(
                            uri,
                            cursor.getString(COLUMN_DOCUMENT_ID)
                        ),
                        fullName = cursor.getString(COLUMN_DISPLAY_NAME),
                        size = cursor.getLong(COLUMN_SIZE),
                        mimeType = documentType
                    )
                )
            }
        }
    }

    override fun listDirectories(directoryInfo: DirectoryInfo): Flow<DirectoryInfo> = flow {
        val uri = directoryInfo.uri as Uri
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(uri, DocumentsContract.getDocumentId(uri))

        context.contentResolver.query(
            childrenUri,
            arrayOf(COLUMN_DOCUMENT_ID, COLUMN_DISPLAY_NAME, COLUMN_MIME_TYPE)
        ) { cursor ->
            while (cursor?.moveToNext() == true) {
                if (cursor.getString(COLUMN_MIME_TYPE) != MIME_TYPE_DIR) continue

                emit(
                    DirectoryInfo(
                        uri = DocumentsContract.buildDocumentUriUsingTree(
                            uri,
                            cursor.getString(COLUMN_DOCUMENT_ID)
                        ),
                        name = cursor.getString(COLUMN_DISPLAY_NAME)
                    )
                )
            }
        }
    }

    @Throws(Exception::class)
    override fun delete(fileInfo: FileInfo) {
        val uri = fileInfo.uri as Uri
        deleteDocument(uri)
    }

    @Throws(Exception::class)
    override fun delete(directoryInfo: DirectoryInfo) {
        val uri = directoryInfo.uri as Uri
        deleteDocument(uri)
    }

    @Throws(Exception::class)
    private fun deleteDocument(uri: Uri) {
        try {
            DocumentsContract.deleteDocument(context.contentResolver, uri)
        } catch (e: IllegalArgumentException) {
            // For some reason, the FileNotFound exception is wrapped in an IllegalArgumentException.
            if (e.message?.contains("java.io.FileNotFoundException") == true) return
            throw e
        }
    }

    override fun relocate(
        sourceFileInfo: FileInfo,
        sourceParentDirectoryInfo: DirectoryInfo,
        targetDirectoryInfo: DirectoryInfo
    ) {
        DocumentsContract.moveDocument(
            context.contentResolver,
            sourceFileInfo.uri as Uri,
            sourceParentDirectoryInfo.uri as Uri,
            targetDirectoryInfo.uri as Uri
        )
    }

    @Throws(FileNotFoundException::class)
    override fun openInputStream(fileInfo: FileInfo): FileInputStream {
        val uri = fileInfo.uri as Uri
        return context.contentResolver.openInputStream(uri) as? FileInputStream ?: throw FileNotFoundException()
    }

    @Throws(FileNotFoundException::class)
    override fun openOutputStream(fileInfo: FileInfo): OutputStream {
        val uri = fileInfo.uri as Uri
        return context.contentResolver.openOutputStream(uri, "rwt") ?: throw FileNotFoundException()
    }

    //region Extension methods

    /**
     * A simple wrapper around [ContentResolver.query] that automatically closes the cursor. Note that `selection` is
     * not available because [Android's FileSystemProvider does not support it](https://stackoverflow.com/a/61214849).
     */
    private inline fun <R> ContentResolver.query(uri: Uri, projection: Array<String>, block: (Cursor?) -> R): R {
        return query(uri, projection, null, null, null).use(block)
    }

    @Throws(IllegalArgumentException::class)
    private fun Cursor.getString(columnName: String): String {
        return getString(getColumnIndexOrThrow(columnName))
    }

    @Throws(IllegalArgumentException::class)
    private fun Cursor.getLong(columnName: String): Long {
        return getLong(getColumnIndexOrThrow(columnName))
    }

    //endregion
}
