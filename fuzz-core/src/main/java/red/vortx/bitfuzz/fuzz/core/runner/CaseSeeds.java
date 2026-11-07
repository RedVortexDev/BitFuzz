// This file is part of BitFuzz.
// SPDX-FileCopyrightText: 2026 RedVortexDev
// SPDX-License-Identifier: Apache-2.0

package red.vortx.bitfuzz.fuzz.core.runner;

import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

/// Derives a seed for each case from the run's seed.
public final class CaseSeeds {

    private static final RandomGeneratorFactory<RandomGenerator> FACTORY = RandomGeneratorFactory.of("L64X128MixRandom");

    private final RandomGenerator master;

    /// Starts the case seeds of a run.
    ///
    /// @param runSeed the seed of the whole run
    public CaseSeeds(final long runSeed) {
        this.master = FACTORY.create(runSeed);
    }

    /// {@return the seed of the next case}
    public long next() {
        return this.master.nextLong();
    }

    /// {@return a random generator for the case with the given seed}
    ///
    /// @param caseSeed the seed of the case
    public static RandomGenerator random(final long caseSeed) {
        return FACTORY.create(caseSeed);
    }

}
