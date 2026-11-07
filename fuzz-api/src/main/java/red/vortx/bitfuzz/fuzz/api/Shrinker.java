// This file is part of BitFuzz.
// SPDX-FileCopyrightText: 2026 RedVortexDev
// SPDX-License-Identifier: Apache-2.0

package red.vortx.bitfuzz.fuzz.api;

import java.util.function.Predicate;

/// Reduces a failing input to a smaller input that still fails the same way.
@FunctionalInterface
public interface Shrinker {

    /// Shrinks the given input.
    ///
    /// @param input      the failing input
    /// @param stillFails whether a candidate input still fails the same way
    /// @return the smallest failing input found
    byte[] shrink(byte[] input, Predicate<byte[]> stillFails);

}
