/*
 * SPDX-FileCopyrightText: 2022 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.domain.vault

import com.google.crypto.tink.StreamingAead
import com.google.crypto.tink.prf.PrfSet
import de.lukaspieper.truvark.crypto.DecryptingFileHandle
import de.lukaspieper.truvark.data.io.DirectoryInfo
import de.lukaspieper.truvark.data.io.FileInfo
import de.lukaspieper.truvark.domain.IndexHandler
import de.lukaspieper.truvark.domain.entities.CipherFileEntity
import de.lukaspieper.truvark.domain.entities.CipherFolderEntity
import de.lukaspieper.truvark.work.DecryptingWorkBundle
import de.lukaspieper.truvark.work.DeletingWorkBundle
import de.lukaspieper.truvark.work.EncryptingWorkBundle
import de.lukaspieper.truvark.work.RelocatingWorkBundle
import de.lukaspieper.truvark.work.Scheduler
import de.lukaspieper.truvark.work.WorkBundle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import logcat.LogPriority
import logcat.asLog
import logcat.logcat
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Instant
import kotlin.uuid.Uuid

public class Vault internal constructor(
    public var config: VaultConfig,
    public val streamingAead: StreamingAead,
    public val prfSet: PrfSet,
    public val fileSystem: VaultFileSystem,
    private val scheduler: Scheduler
) {
    private val folderIndexHandler = IndexHandler.FolderIndexHandler(streamingAead, fileSystem, config)

    private val fileIndexHandlers = mutableMapOf<Uuid, IndexHandler.FileIndexHandler>()

    public val id: Uuid
        get() = config.id

    public val name: String
        get() = config.name

    public val rootFolder: CipherFolderEntity
        get() = CipherFolderEntity(
            id = Uuid.NIL,
            displayName = name,
            parentFolderId = Uuid.NIL,
            creationTimestamp = Instant.DISTANT_PAST
        )

    @Throws(NoSuchElementException::class)
    public fun findCipherFolderEntity(folderId: Uuid): CipherFolderEntity {
        return when (folderId) {
            Uuid.NIL -> rootFolder
            else -> folderIndexHandler.items.value.firstOrNull { it.id == folderId }
                ?: throw NoSuchElementException("Folder with ID $folderId not found")
        }
    }

    /**
     * Returns a flow containing the subfolders of the given [folder]. Use [rootFolder] to get the root level folders.
     */
    public fun findCipherFolderEntitySubfolders(folder: CipherFolderEntity): Flow<List<CipherFolderEntity>> {
        return folderIndexHandler.items.map { list ->
            list.filter { folderEntity -> folderEntity.parentFolderId == folder.id }
                .sortedBy { folderEntity -> folderEntity.displayName }
        }
    }

    public fun findCipherFileEntitiesForFolder(folder: CipherFolderEntity): Flow<List<CipherFileEntity>> {
        if (folder.id == Uuid.NIL) {
            return emptyFlow()
        }

        val indexHandler = fileIndexHandlers.getOrCreate(folder.id)
        return indexHandler.items.map { list ->
            list.sortedByDescending { fileEntity -> fileEntity.creationTimestamp }
        }
    }

    // No directory is created here to avoid IO. So this has no effect on VaultFileSystem's directory cache.
    public suspend fun createFolder(displayName: String, parentFolder: CipherFolderEntity): CipherFolderEntity {
        val newFolder = CipherFolderEntity(
            id = Uuid.random(),
            displayName = displayName,
            parentFolderId = parentFolder.id,
            creationTimestamp = Clock.System.now()
        )

        folderIndexHandler.addItems(newFolder)
        logcat(LogPriority.INFO) { "Folder (${newFolder.id}) created" }

        return newFolder
    }

    @Throws(IllegalArgumentException::class)
    public suspend fun renameFolder(folder: CipherFolderEntity, displayName: String) {
        if (folder.displayName == displayName) return

        val updatedFolder = folder.copy(displayName = displayName)
        folderIndexHandler.updateItem(folder, updatedFolder)

        logcat(LogPriority.INFO) { "Folder (${updatedFolder.id}) updated" }
    }

    /**
     * Schedules the encryption of the given [sources] to the given [destination] folder. If [deleteSources] is true,
     * the source files will be deleted after successful encryption.
     *
     * The [Scheduler] might **require** some [properties] depending on the platform.
     */
    @Throws(IllegalArgumentException::class)
    public fun scheduleFileEncryption(
        properties: WorkBundle.Properties,
        sources: List<() -> FileInfo>,
        destination: CipherFolderEntity,
        deleteSources: Boolean = false
    ) {
        if (sources.isEmpty()) return

        val timestamp = Clock.System.now()
        scheduler.schedule(
            workBundle = EncryptingWorkBundle(
                properties = properties,
                streamingAead = streamingAead,
                vault = this,
                // Utilizing the index ensures unique timestamps and thereby the same order as the source list.
                sources = sources.mapIndexed { index, source -> Pair(source, timestamp + index.milliseconds) },
                destination = destination,
                destinationIndexHandler = fileIndexHandlers.getOrCreate(destination.id),
                deleteSources = deleteSources
            )
        )
    }

    // TODO: This approach leads to multiple notifications for a single user action.
    @Throws(IllegalArgumentException::class)
    public suspend fun scheduleDirectoryEncryption(
        properties: WorkBundle.Properties,
        source: DirectoryInfo,
        destination: CipherFolderEntity,
        deleteSources: Boolean = false
    ) {
        val folder = createFolder(source.name, destination)

        scheduleFileEncryption(
            properties = properties,
            sources = fileSystem.listFiles(source).map { file -> { file } }.toList(),
            destination = folder,
            deleteSources = deleteSources
        )

        fileSystem.listDirectories(source).collect { sourceDirectory ->
            scheduleDirectoryEncryption(
                properties = properties,
                source = sourceDirectory,
                destination = folder,
                deleteSources = deleteSources
            )
        }
    }

    /**
     * Schedules the decryption of the given [folders] and [files]. Writes decrypted copies to a matching directory
     * inside [VaultFileSystem.decryptionRootDirectory]. All [folders] and [files] **must** have the same parent
     * folder and will not be modified or deleted.
     *
     * The [Scheduler] might **require** some [properties] depending on the platform.
     */
    @Throws(IllegalArgumentException::class)
    public fun scheduleDecryption(
        properties: WorkBundle.Properties,
        files: Set<CipherFileEntity>,
        folders: Set<CipherFolderEntity>
    ) {
        val parentFolderId = buildSet {
            files.mapTo(this) { it.folderId }
            folders.mapTo(this) { it.parentFolderId }
        }.singleOrNull() ?: throw IllegalArgumentException("All files and folders must have the same parent folder")

        scheduler.schedule(
            workBundle = DecryptingWorkBundle(
                properties = properties,
                vault = this,
                parentFolder = findCipherFolderEntity(parentFolderId),
                files = files.toList(),
                folders = folders.toList(),
            )
        )
    }

    /**
     * Schedules the deletion of the given [folders] and [files]. Removes the physical files and folders as well as
     * updates the index files. All [folders] and [files] **must** have the same parent folder.
     *
     * The [Scheduler] might **require** some [properties] depending on the platform.
     */
    @Throws(IllegalArgumentException::class)
    public fun scheduleDeletion(
        properties: WorkBundle.Properties,
        files: Set<CipherFileEntity>,
        folders: Set<CipherFolderEntity>
    ) {
        val parentFolderId = buildSet {
            files.mapTo(this) { it.folderId }
            folders.mapTo(this) { it.parentFolderId }
        }.singleOrNull() ?: throw IllegalArgumentException("All files and folders must have the same parent folder")

        scheduler.schedule(
            workBundle = DeletingWorkBundle(
                properties = properties,
                vault = this,
                folderIndexHandler = folderIndexHandler,
                fileIndexHandler = fileIndexHandlers.getOrCreate(parentFolderId),
                files = files.toList(),
                folders = folders.toList()
            )
        )
    }

    /**
     * Schedules the relocation of the given [folders] and [files] to the given [destination] folder.
     *
     * Only relocating files will lead to heavy IO operations. Relocating folders will only change the index files.
     * During the relocation process, the files and folders will **not** be decrypted and re-encrypted.
     *
     * The [Scheduler] might **require** some [properties] depending on the platform.
     */
    @Throws(IllegalArgumentException::class)
    public fun scheduleRelocation(
        properties: WorkBundle.Properties,
        destination: CipherFolderEntity,
        files: Set<CipherFileEntity>,
        folders: Set<CipherFolderEntity>
    ) {
        val parentFolderId = buildSet {
            files.mapTo(this) { it.folderId }
            folders.mapTo(this) { it.parentFolderId }
        }.singleOrNull() ?: throw IllegalArgumentException("All files and folders must have the same parent folder")

        scheduler.schedule(
            workBundle = RelocatingWorkBundle(
                properties = properties,
                fileSystem = fileSystem,
                folderIndexHandler = folderIndexHandler,
                sourceFileIndexHandler = fileIndexHandlers.getOrCreate(parentFolderId),
                destinationFileIndexHandler = fileIndexHandlers.getOrCreate(destination.id),
                destination = destination,
                files = files.toList(),
                folders = folders.toList()
            )
        )
    }

    public fun createDecryptingFileHandle(file: FileInfo): DecryptingFileHandle {
        return DecryptingFileHandle(
            fileSystem = fileSystem,
            streamingAead = streamingAead,
            file = file,
            associatedData = id.toByteArray() + Uuid.parseHex(file.fullName).toByteArray()
        )
    }

    @Throws(Exception::class)
    public fun updateName(updatedName: String) {
        if (name == updatedName) return
        val updatedConfig = config.copy(name = updatedName)

        try {
            fileSystem.openOutputStream(fileSystem.vaultFile).use { outputStream ->
                outputStream.write(updatedConfig.toByteArray())
            }

            // Verify write
            fileSystem.openInputStream(fileSystem.vaultFile).use { inputStream ->
                val readVaultConfig = VaultConfig.fromByteArray(inputStream.readBytes())
                check(readVaultConfig.name == updatedName)
                config = readVaultConfig
            }
        } catch (e: Exception) {
            logcat(LogPriority.ERROR) { e.asLog() }

            logcat { "Trying to restore previous vault config." }
            fileSystem.openOutputStream(fileSystem.vaultFile).use { outputStream ->
                outputStream.write(config.toByteArray())
            }

            throw e
        }
    }

    private fun MutableMap<Uuid, IndexHandler.FileIndexHandler>.getOrCreate(
        folderId: Uuid
    ): IndexHandler.FileIndexHandler {
        return this.getOrPut(folderId) {
            IndexHandler.FileIndexHandler(
                streamingAead = streamingAead,
                vaultFileSystem = fileSystem,
                vaultConfig = config,
                folder = findCipherFolderEntity(folderId)
            )
        }
    }
}
