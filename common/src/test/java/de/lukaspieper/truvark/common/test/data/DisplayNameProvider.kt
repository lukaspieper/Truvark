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

class DisplayNameProvider : ArgumentsProvider {

    override fun provideArguments(context: ExtensionContext?): Stream<out Arguments> {
        return Stream.of(
            Arguments.of("!§\$%&/()=?`*+'#-_.:,;<>|"),
            Arguments.of("display name with space"),
        )
    }
}
