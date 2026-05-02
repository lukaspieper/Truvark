/*
 * SPDX-FileCopyrightText: 2025 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.test.data

import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.support.ParameterDeclarations
import java.util.stream.Stream
import kotlin.random.Random
import kotlin.uuid.Uuid

/**
 * [ArgumentsProvider] for common file data. Provides valid **file names** and **sizes** using partly random values.
 */
class FileDataProvider : ArgumentsProvider {

    override fun provideArguments(
        parameters: ParameterDeclarations?,
        context: ExtensionContext?
    ): Stream<out Arguments> {
        val uuid = Uuid.random().toHexString()

        return Stream.of(
            Arguments.of(uuid, Random.nextInt(1, 1000)),
            Arguments.of("$uuid.file", Random.nextInt(1, 1000)),
            Arguments.of("with space.file", Random.nextInt(1, 1000)),
        )
    }
}
