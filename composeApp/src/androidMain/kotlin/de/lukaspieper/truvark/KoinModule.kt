/*
 * SPDX-FileCopyrightText: 2026 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark

import android.content.Context
import coil3.ImageLoader
import coil3.request.CachePolicy
import coil3.util.DebugLogger
import de.lukaspieper.truvark.crypto.Argon2
import de.lukaspieper.truvark.data.io.AndroidFileSystem
import de.lukaspieper.truvark.data.io.FileSystem
import de.lukaspieper.truvark.data.preferences.PersistentPreferences
import de.lukaspieper.truvark.domain.crypto.AndroidArgon2
import de.lukaspieper.truvark.domain.crypto.BiometricCryptoProvider
import de.lukaspieper.truvark.domain.crypto.decryption.coil.CipherFileFetcher
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
import logcat.LogPriority.DEBUG
import logcat.logcat
import org.koin.core.context.GlobalContext
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.plugin.module.dsl.factory
import org.koin.plugin.module.dsl.single
import org.koin.plugin.module.dsl.viewModel
import org.koin.plugin.module.dsl.worker

public object KoinModule {

    private const val VAULT_SCOPE = "unlocked_vault"

    // Koin does not support Dagger's '@Reusable' feature (https://github.com/InsertKoinIO/koin/issues/1378)
    public val module: Module = module {

        single<PersistentPreferences>()
        single<WorkScheduler>() bind Scheduler::class

        factory<AndroidFileSystem>() bind FileSystem::class
        factory<BiometricCryptoProvider>()
        factory<AndroidArgon2>() bind Argon2::class
        factory<VaultFactory>()

        viewModel<LauncherViewModel>()
        worker<UniversalWorker>()

        scope(named(VAULT_SCOPE)) {

            // Required to satisfy the Koin compiler plugin.
            scoped<Vault> { error("Vault must be declared during scope creation") } bind Vault::class

            scoped {
                val context = get<Context>()
                val vault = get<Vault>()

                ImageLoader.Builder(context)
                    .components {
                        add(CipherFileFetcher.Factory(vault))
                        add(FileInfoKeyer())
                        add(ThumbnailCacheInterceptor(vault, context.cacheDir))
                    }
                    .diskCachePolicy(CachePolicy.DISABLED)
                    .memoryCachePolicy(CachePolicy.DISABLED)
                    .apply {
                        if (BuildConfig.DEBUG) {
                            logger(DebugLogger())
                        }
                    }
                    .build()
            } bind ImageLoader::class

            viewModel<BrowserViewModel>()
            viewModel<AppSettingsViewModel>()
            viewModel<VaultSettingsViewModel>()
            viewModel<PresenterViewModel>()
        }
    }

    /**
     * Tries to create a new Koin scope for the given vault. If a scope with the same ID already exists, does nothing.
     */
    public fun createUnlockedVaultScopeOrIgnore(vault: Vault) {
        val scopeId = vault.id.toHexString()

        with(GlobalContext.get()) {
            if (getScopeOrNull(scopeId) == null) {
                createScope(scopeId, named(VAULT_SCOPE)).declare(vault)

                logcat(DEBUG) { "Created new scope for vault ${vault.id}." }
            } else {
                logcat(DEBUG) { "Scope for vault ${vault.id} already exists, not creating a new one." }
            }
        }
    }
}
