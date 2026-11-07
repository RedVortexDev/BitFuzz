// This file is part of BitFuzz.
// SPDX-FileCopyrightText: 2026 RedVortexDev
// SPDX-License-Identifier: Apache-2.0

package red.vortx.bitfuzz.fuzz.core.mutator;

import red.vortx.bitfuzz.fuzz.api.Mutator;

import java.util.random.RandomGenerator;

/// Replaces a random byte with an interesting one, like `00`, `7f`, `80` or `ff`.
///
/// Checks: flags, general corruption
public class InterestingByteMutator implements Mutator {

    private static final byte[] INTERESTING_BYTES = {
            Byte.MIN_VALUE,
            -1,
            0, 1,
            16, 32, 64, 100,
            Byte.MAX_VALUE
    };

    @Override
    public byte[] mutate(final byte[] input, final RandomGenerator random) {
        final byte[] copy = input.clone();
        if (copy.length == 0) {
            return copy;
        }

        final int index = random.nextInt(input.length);
        final byte interestingByte = INTERESTING_BYTES[random.nextInt(INTERESTING_BYTES.length)];
        copy[index] = interestingByte;

        return copy;
    }

}
