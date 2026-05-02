/*
 * SPDX-FileCopyrightText: 2022 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import de.lukaspieper.truvark.crypto.Argon2
import de.lukaspieper.truvark.data.database.DatabaseFileSynchronization
import de.lukaspieper.truvark.data.io.AndroidFileSystem
import de.lukaspieper.truvark.data.io.FileSystem
import de.lukaspieper.truvark.data.preferences.PersistentPreferences
import de.lukaspieper.truvark.domain.AndroidThumbnailProvider
import de.lukaspieper.truvark.domain.IdGenerator
import de.lukaspieper.truvark.domain.ThumbnailProvider
import de.lukaspieper.truvark.domain.crypto.AndroidArgon2
import de.lukaspieper.truvark.domain.crypto.BiometricCryptoProvider
import de.lukaspieper.truvark.domain.vault.VaultFactory
import de.lukaspieper.truvark.work.Scheduler
import de.lukaspieper.truvark.work.WorkScheduler
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
public object ApplicationModule {

    @Reusable
    @Provides
    public fun provideAndroidFileSystem(@ApplicationContext appContext: Context): AndroidFileSystem {
        return AndroidFileSystem(appContext)
    }

    @Provides
    public fun provideFileSystem(fileSystem: AndroidFileSystem): FileSystem {
        return fileSystem
    }

    @Singleton
    @Provides
    public fun providePersistentPreferences(@ApplicationContext appContext: Context): PersistentPreferences {
        return PersistentPreferences(appContext)
    }

    @Singleton
    @Provides
    public fun provideWorkScheduler(@ApplicationContext appContext: Context): WorkScheduler {
        return WorkScheduler(appContext)
    }

    @Provides
    public fun provideScheduler(workScheduler: WorkScheduler): Scheduler {
        return workScheduler
    }

    @Singleton
    @Provides
    public fun provideThumbnailProvider(@ApplicationContext appContext: Context): ThumbnailProvider {
        return AndroidThumbnailProvider(appContext)
    }

    @Reusable
    @Provides
    public fun provideBiometricCryptoProvider(@ApplicationContext appContext: Context): BiometricCryptoProvider {
        return BiometricCryptoProvider(appContext)
    }

    @Provides
    public fun provideIdGenerator(): IdGenerator {
        return IdGenerator.Default
    }

    @Reusable
    @Provides
    public fun provideArgon2(): Argon2 {
        return AndroidArgon2()
    }

    @Provides
    public fun provideVaultFactory(
        argon2: Argon2,
        fileSystem: FileSystem,
        idGenerator: IdGenerator,
        thumbnailProvider: ThumbnailProvider,
        scheduler: Scheduler
    ): VaultFactory {
        return VaultFactory(
            argon2 = argon2,
            fileSystem = fileSystem,
            idGenerator = idGenerator,
            thumbnailProvider = thumbnailProvider,
            scheduler = scheduler
        )
    }

    @Provides
    public fun provideDatabaseFileSynchronization(
        workScheduler: WorkScheduler,
        fileSystem: FileSystem
    ): DatabaseFileSynchronization {
        return DatabaseFileSynchronization(workScheduler, fileSystem)
    }
}
