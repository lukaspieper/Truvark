/*
 * SPDX-FileCopyrightText: 2022 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.di

import android.content.Context
import android.content.Intent
import coil3.ImageLoader
import coil3.request.CachePolicy
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import de.lukaspieper.truvark.BuildConfig
import de.lukaspieper.truvark.domain.crypto.decryption.coil.CipherFileFetcher
import de.lukaspieper.truvark.domain.crypto.decryption.coil.CoilLoggerAdapter
import de.lukaspieper.truvark.domain.crypto.decryption.coil.FileInfoKeyer
import de.lukaspieper.truvark.domain.crypto.decryption.coil.ThumbnailCacheInterceptor
import de.lukaspieper.truvark.domain.vault.Vault
import javax.inject.Singleton

// TODO: Get rid of this module
@Module
@InstallIn(SingletonComponent::class)
public object VaultModule {
    private var vault: Vault? = null

    public fun initializeVaultModule(vault: Vault) {
        this.vault = vault
    }

    @Provides
    public fun provideVault(@ApplicationContext appContext: Context): Vault {
        if (vault == null) {
            // I can't reproduce this case, and it should never happen, but crashes have been reported by Google Play
            // on devices known to kill apps in the background (https://dontkillmyapp.com/). Attempt app restart.
            appContext.packageManager.getLaunchIntentForPackage(appContext.packageName)?.let { intent ->
                val mainIntent = Intent.makeRestartActivityTask(intent.component)
                appContext.startActivity(mainIntent)
                Runtime.getRuntime().exit(0)
            }
        }

        return vault ?: error("Vault not initialized")
    }

    @Singleton
    @Provides
    public fun provideImageLoader(@ApplicationContext appContext: Context, vault: Vault): ImageLoader {
        val thumbnailCache = ThumbnailCacheInterceptor(vault, appContext.cacheDir)

        val builder = ImageLoader.Builder(appContext)
            .components {
                add(CipherFileFetcher.Factory(vault))
                add(FileInfoKeyer())
                add(thumbnailCache)
            }
            // Coil's disk cache seems to be solely designed for caching the resulting data from a network request.
            // It does not seem to have built-in support for caching processed data like thumbnails (of videos).
            .diskCachePolicy(CachePolicy.DISABLED)
            .memoryCachePolicy(CachePolicy.DISABLED)

        if (BuildConfig.DEBUG) {
            builder.logger(CoilLoggerAdapter())
        }

        return builder.build()
    }
}
