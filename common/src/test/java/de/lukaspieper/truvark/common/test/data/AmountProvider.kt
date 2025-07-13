/*
 * SPDX-FileCopyrightText: 2025 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.common.test.data

import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import java.util.stream.Stream

class AmountProvider : ArgumentsProvider {

    override fun provideArguments(context: ExtensionContext?): Stream<out Arguments> {
        return intArrayOf(0, 1, 2, 5, 10, 100).map { Arguments.of(it) }.stream()
    }
}
