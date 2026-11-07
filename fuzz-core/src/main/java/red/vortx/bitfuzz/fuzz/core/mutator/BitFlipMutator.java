// This file is part of BitFuzz.
// SPDX-FileCopyrightText: 2026 RedVortexDev
// SPDX-License-Identifier: Apache-2.0

package red.vortx.bitfuzz.fuzz.core.mutator;

import red.vortx.bitfuzz.fuzz.api.Mutator;

import java.util.random.RandomGenerator;

/// Flips one random bit.
///
/// Checks: general corruption
public class BitFlipMutator implements Mutator {

    @Override
    public byte[] mutate(final byte[] input, final RandomGenerator random) {
        final byte[] copy = input.clone();
        if (copy.length == 0) {
            return copy;
        }

        final int index = random.nextInt(copy.length);
        final int mask = 1 << random.nextInt(Byte.SIZE);
        copy[index] = (byte) (copy[index] ^ mask);

        return copy;
    }

}
