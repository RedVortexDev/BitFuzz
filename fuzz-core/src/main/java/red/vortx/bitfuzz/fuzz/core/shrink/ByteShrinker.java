// This file is part of BitFuzz.
// SPDX-FileCopyrightText: 2026 RedVortexDev
// SPDX-License-Identifier: Apache-2.0

package red.vortx.bitfuzz.fuzz.core.shrink;

import red.vortx.bitfuzz.fuzz.api.Shrinker;

import java.util.function.Predicate;

/// Shrinks inputs by removing chunks of bytes, then zeroing single bytes.
///
/// @param maxAttempts the maximum number of times `stillFails` is called
public record ByteShrinker(int maxAttempts) implements Shrinker {

    public static final int DEFAULT_MAX_ATTEMPTS = 5_000;

    public ByteShrinker {
        if (maxAttempts < 0) {
            throw new IllegalArgumentException("maxAttempts must not be negative");
        }
    }

    public ByteShrinker() {
        this(DEFAULT_MAX_ATTEMPTS);
    }

    @Override
    public byte[] shrink(final byte[] input, final Predicate<byte[]> stillFails) {
        final Attempts attempts = new Attempts(this.maxAttempts, stillFails);
        final byte[] removed = this.removeChunks(input.clone(), attempts);
        return this.zeroBytes(removed, attempts);
    }

    private byte[] removeChunks(final byte[] input, final Attempts attempts) {
        byte[] current = input;
        int chunk = Math.max(1, current.length / 2);

        while (chunk >= 1 && attempts.remaining()) {
            boolean progress = false;
            int start = 0;

            while (start + chunk <= current.length && attempts.remaining()) {
                final byte[] candidate = withoutRange(current, start, start + chunk);

                if (attempts.test(candidate)) {
                    // Keep the smaller input and retry the same position: later bytes moved into it
                    current = candidate;
                    progress = true;
                } else {
                    start += chunk;
                }
            }

            if (!progress) {
                chunk /= 2;
            }
        }

        return current;
    }

    private byte[] zeroBytes(final byte[] input, final Attempts attempts) {
        final byte[] current = input.clone();

        for (int i = 0; i < current.length && attempts.remaining(); i++) {
            if (current[i] == 0) {
                continue;
            }

            final byte original = current[i];
            current[i] = 0;
            if (!attempts.test(current.clone())) {
                current[i] = original;
            }
        }

        return current;
    }

    private static byte[] withoutRange(final byte[] input, final int start, final int end) {
        final byte[] result = new byte[input.length - (end - start)];
        System.arraycopy(input, 0, result, 0, start);
        System.arraycopy(input, end, result, start, input.length - end);
        return result;
    }

    private static final class Attempts {

        private final Predicate<byte[]> stillFails;
        private int left;

        private Attempts(final int max, final Predicate<byte[]> stillFails) {
            this.left = max;
            this.stillFails = stillFails;
        }

        private boolean remaining() {
            return this.left > 0;
        }

        private boolean test(final byte[] candidate) {
            this.left--;
            return this.stillFails.test(candidate);
        }

    }

}
