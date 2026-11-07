// This file is part of BitFuzz.
// SPDX-FileCopyrightText: 2026 RedVortexDev
// SPDX-License-Identifier: Apache-2.0

package red.vortx.bitfuzz.fuzz.api;

/// Runs a fuzzing property.
@FunctionalInterface
public interface FuzzRunner {

    /// Runs the property.
    ///
    /// @param iterations the maximum number of inputs to try
    /// @param seed       the seed for the run
    /// @return the outcome of the run
    FuzzResult run(int iterations, long seed);

}
