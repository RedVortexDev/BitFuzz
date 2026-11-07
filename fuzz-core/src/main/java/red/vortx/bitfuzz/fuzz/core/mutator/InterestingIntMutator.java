// This file is part of BitFuzz.
// SPDX-FileCopyrightText: 2026 RedVortexDev
// SPDX-License-Identifier: Apache-2.0

package red.vortx.bitfuzz.fuzz.core.mutator;

import red.vortx.bitfuzz.fuzz.api.Mutator;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.random.RandomGenerator;

/// Overwrites 4 bytes at a random position with an interesting int, in either byte order.
///
/// Inputs shorter than 4 bytes are returned unchanged.
///
/// Checks: fake lengths
public class InterestingIntMutator implements Mutator {

    private static final int[] INTERESTING_INTS = {
            Integer.MIN_VALUE,
            -100663046, -32769, 32768,
            0, -1,
            0xFFFF,
            0x10000,
            100663045,
            Integer.MAX_VALUE
    };

    @Override
    public byte[] mutate(final byte[] input, final RandomGenerator random) {
        final byte[] copy = input.clone();
        if (copy.length < Integer.BYTES) {
            return copy;
        }

        final int index = random.nextInt(copy.length - Integer.BYTES + 1);
        final int interestingInt = INTERESTING_INTS[random.nextInt(INTERESTING_INTS.length)];
        final ByteOrder order = random.nextBoolean() ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN;

        ByteBuffer.wrap(copy).order(order).putInt(index, interestingInt);
        return copy;
    }

}
