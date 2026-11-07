// This file is part of BitFuzz.
// SPDX-FileCopyrightText: 2026 RedVortexDev
// SPDX-License-Identifier: Apache-2.0

package red.vortx.bitfuzz.fuzz.api;

import java.util.random.RandomGenerator;

/// Produces random values of one type.
///
/// @param <T> the type of the generated values
@FunctionalInterface
public interface Generator<T> {

    /// Generates a random value.
    ///
    /// @param random the source of randomness
    /// @return a random value
    T generate(RandomGenerator random);

}
