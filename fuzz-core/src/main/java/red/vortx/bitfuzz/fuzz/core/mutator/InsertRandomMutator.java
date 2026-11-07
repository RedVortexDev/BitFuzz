// This file is part of BitFuzz.
// SPDX-FileCopyrightText: 2026 RedVortexDev
// SPDX-License-Identifier: Apache-2.0

package red.vortx.bitfuzz.fuzz.core.mutator;

import red.vortx.bitfuzz.fuzz.api.Mutator;

import java.util.random.RandomGenerator;

/// Inserts 1 to [#MAX_INSERTED_BYTES] random bytes at a random position.
///
/// Checks: fields shifted out of place, trailing garbage
public class InsertRandomMutator implements Mutator {

    public static final int MAX_INSERTED_BYTES = 8;

    @Override
    public byte[] mutate(final byte[] input, final RandomGenerator random) {
        final int position = random.nextInt(input.length + 1);
        final byte[] inserted = new byte[random.nextInt(1, MAX_INSERTED_BYTES + 1)];
        random.nextBytes(inserted);

        final byte[] copy = new byte[input.length + inserted.length];
        System.arraycopy(input, 0, copy, 0, position);
        System.arraycopy(inserted, 0, copy, position, inserted.length);
        System.arraycopy(input, position, copy, position + inserted.length, input.length - position);

        return copy;
    }

}
