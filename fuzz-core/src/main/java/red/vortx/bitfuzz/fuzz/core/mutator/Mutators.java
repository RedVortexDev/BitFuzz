// This file is part of BitFuzz.
// SPDX-FileCopyrightText: 2026 RedVortexDev
// SPDX-License-Identifier: Apache-2.0

package red.vortx.bitfuzz.fuzz.core.mutator;

import red.vortx.bitfuzz.fuzz.api.Mutator;

import java.util.List;

/// The standard set of mutators.
public enum Mutators {
    ;

    private static final List<Mutator> STANDARD = List.of(
            new BitFlipMutator(),
            new InterestingByteMutator(),
            new InterestingIntMutator(),
            new TruncateMutator(),
            new DeleteRangeMutator(),
            new InsertRandomMutator(),
            new DuplicateChunkMutator()
    );

    /// {@return every built-in mutator}
    public static List<Mutator> standard() {
        return STANDARD;
    }

}
