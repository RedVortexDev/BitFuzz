// This file is part of BitFuzz.
// SPDX-FileCopyrightText: 2026 RedVortexDev
// SPDX-License-Identifier: Apache-2.0

package red.vortx.bitfuzz.fuzz.core.generator.impl;

import red.vortx.bitfuzz.fuzz.api.Generator;

import java.util.random.RandomGenerator;

/// Generates a random enum constant.
///
/// @param constants the enum constants
/// @param <E>       the enum type
public record EnumGenerator<E>(E[] constants) implements Generator<E> {

    public EnumGenerator {
        if (constants.length == 0) {
            throw new IllegalArgumentException("Cannot generate values of an enum with no constants");
        }
    }

    @Override
    public E generate(final RandomGenerator random) {
        final int index = random.nextInt(this.constants.length);
        return this.constants[index];
    }

}
