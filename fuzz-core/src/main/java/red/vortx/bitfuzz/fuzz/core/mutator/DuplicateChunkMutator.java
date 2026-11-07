// This file is part of BitFuzz.
// SPDX-FileCopyrightText: 2026 RedVortexDev
// SPDX-License-Identifier: Apache-2.0

package red.vortx.bitfuzz.fuzz.core.mutator;

import red.vortx.bitfuzz.fuzz.api.Mutator;

import java.util.random.RandomGenerator;

/// Copies a chunk of up to [#MAX_CHUNK_SIZE] bytes to a random position.
///
/// Checks: repeated structures, duplicate map keys
public class DuplicateChunkMutator implements Mutator {

    public static final int MAX_CHUNK_SIZE = 32;

    @Override
    public byte[] mutate(final byte[] input, final RandomGenerator random) {
        if (input.length == 0) {
            return input.clone();
        }

        final int start = random.nextInt(input.length);
        final int end = random.nextInt(start + 1, Math.min(input.length, start + MAX_CHUNK_SIZE) + 1);
        final int chunkSize = end - start;
        final int position = random.nextInt(input.length + 1);

        final byte[] copy = new byte[input.length + chunkSize];
        System.arraycopy(input, 0, copy, 0, position);
        System.arraycopy(input, start, copy, position, chunkSize);
        System.arraycopy(input, position, copy, position + chunkSize, input.length - position);

        return copy;
    }

}
