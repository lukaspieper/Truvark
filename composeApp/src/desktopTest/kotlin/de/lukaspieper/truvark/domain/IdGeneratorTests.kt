/*
 * SPDX-FileCopyrightText: 2022 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.domain

import de.lukaspieper.truvark.test.data.AmountProvider
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource

class IdGeneratorTests {

    @ParameterizedTest
    @ArgumentsSource(AmountProvider::class) // amount of characters = length
    fun `createStringId returns valid id`(length: Int) {
        // Act
        val id = IdGenerator.Default.createStringId(length)

        // Assert
        assertAll(
            { assertEquals(length, id.length) },
            { assertTrue(id.contains(Regex("[${IdGenerator.Default.ALLOWED_CHARS}]*"))) }
        )
    }

    @Test
    fun `createStringId returns different ids`() {
        // Arrange
        val length = 20
        val amount = 100_000
        val set = HashSet<String>(amount)

        // Act
        repeat(amount) {
            set.add(IdGenerator.Default.createStringId(length))
        }

        // Assert
        assertEquals(amount, set.size)
    }
}
