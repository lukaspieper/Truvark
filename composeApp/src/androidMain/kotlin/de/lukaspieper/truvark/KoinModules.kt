/*
 * SPDX-FileCopyrightText: 2026 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark

import android.content.Context
import coil3.ImageLoader
import coil3.request.CachePolicy
import de.lukaspieper.truvark.crypto.Argon2
import de.lukaspieper.truvark.data.io.AndroidFileSystem
import de.lukaspieper.truvark.data.io.FileSystem
import de.lukaspieper.truvark.data.preferences.PersistentPreferences
import de.lukaspieper.truvark.domain.crypto.AndroidArgon2
import de.lukaspieper.truvark.domain.crypto.BiometricCryptoProvider
import de.lukaspieper.truvark.domain.crypto.decryption.coil.CipherFileFetcher
import de.lukaspieper.truvark.domain.crypto.decryption.coil.CoilLoggerAdapter
import de.lukaspieper.truvark.domain.crypto.decryption.coil.FileInfoKeyer
import de.lukaspieper.truvark.domain.crypto.decryption.coil.ThumbnailCacheInterceptor
import de.lukaspieper.truvark.domain.vault.Vault
import de.lukaspieper.truvark.domain.vault.VaultFactory
import de.lukaspieper.truvark.ui.views.browser.BrowserViewModel
import de.lukaspieper.truvark.ui.views.launcher.LauncherViewModel
import de.lukaspieper.truvark.ui.views.presenter.PresenterViewModel
import de.lukaspieper.truvark.ui.views.settings.app.AppSettingsViewModel
import de.lukaspieper.truvark.ui.views.settings.vault.VaultSettingsViewModel
import de.lukaspieper.truvark.work.Scheduler
import de.lukaspieper.truvark.work.UniversalWorker
import de.lukaspieper.truvark.work.WorkScheduler
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.plugin.module.dsl.factory
import org.koin.plugin.module.dsl.single
import org.koin.plugin.module.dsl.viewModel
import org.koin.plugin.module.dsl.worker

public object KoinModules {

    // Koin does not support Dagger's '@Reusable' feature (https://github.com/InsertKoinIO/koin/issues/1378)
    public val appModule: Module = module {
        single<PersistentPreferences>()
        single<WorkScheduler>() bind Scheduler::class

        factory<AndroidFileSystem>() bind FileSystem::class
        factory<BiometricCryptoProvider>()
        factory<AndroidArgon2>() bind Argon2::class
        factory<VaultFactory>()

        viewModel<LauncherViewModel>()
        worker<UniversalWorker>()
    }

    // TODO: Migrate to scoped.
    public val vaultModule: Module = module {
        factory { vault ?: error("Vault not initialized") } bind Vault::class

        single {
            val context: Context = get()
            val vault: Vault = get()

            val thumbnailCache = ThumbnailCacheInterceptor(
                vault,
                context.cacheDir
            )

            ImageLoader.Builder(context)
                .components {
                    add(CipherFileFetcher.Factory(vault))
                    add(FileInfoKeyer())
                    add(thumbnailCache)
                }
                .diskCachePolicy(CachePolicy.DISABLED)
                .memoryCachePolicy(CachePolicy.DISABLED)
                .apply {
                    if (BuildConfig.DEBUG) {
                        logger(CoilLoggerAdapter())
                    }
                }
                .build()
        } bind ImageLoader::class

        viewModel<BrowserViewModel>()
        viewModel<AppSettingsViewModel>()
        viewModel<VaultSettingsViewModel>()
        viewModel<PresenterViewModel>()
    }

    public var vault: Vault? = null
}
