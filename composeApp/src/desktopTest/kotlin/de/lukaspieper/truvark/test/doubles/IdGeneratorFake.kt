/*
 * SPDX-FileCopyrightText: 2023 Lukas Pieper
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package de.lukaspieper.truvark.test.doubles

import de.lukaspieper.truvark.domain.IdGenerator

class IdGeneratorFake(ids: Array<String>) : IdGenerator {

    private var idIterator = ids.iterator()

    override fun createStringId(length: Int): String {
        return idIterator.next()
    }
}
