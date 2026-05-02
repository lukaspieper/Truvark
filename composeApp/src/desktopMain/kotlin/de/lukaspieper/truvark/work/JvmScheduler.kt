/*
 * SPDX-FileCopyrightText: 2025 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.work

import de.lukaspieper.truvark.domain.vault.Vault

public class JvmScheduler : Scheduler() {

    override fun schedule(
        workBundle: WorkBundle,
        metadata: SchedulerMetadata
    ) {
        TODO("Not yet implemented")
    }

    override fun onVaultChanged(vault: Vault) {
        TODO("Not yet implemented")
    }
}
