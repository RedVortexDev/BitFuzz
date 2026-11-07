// This file is part of BitFuzz.
// SPDX-FileCopyrightText: 2026 RedVortexDev
// SPDX-License-Identifier: Apache-2.0

package red.vortx.bitfuzz.fuzz.core.generator.impl;

import red.vortx.bitfuzz.fuzz.api.Generator;

import java.util.Objects;
import java.util.Optional;
import java.util.random.RandomGenerator;

/// Generates empty and present optionals, half the time each.
///
/// @param value the generator for present values
/// @param <T>   the type of the value
public record OptionalGenerator<T>(Generator<T> value) implements Generator<Optional<T>> {

    public OptionalGenerator {
        Objects.requireNonNull(value, "value");
    }

    @Override
    public Optional<T> generate(final RandomGenerator random) {
        return random.nextBoolean() ? Optional.empty() : Optional.of(this.value.generate(random));
    }

}
