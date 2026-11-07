// This file is part of BitFuzz.
// SPDX-FileCopyrightText: 2026 RedVortexDev
// SPDX-License-Identifier: Apache-2.0

package red.vortx.bitfuzz.fuzz.api;

import java.util.Arrays;
import java.util.HexFormat;
import java.util.Objects;

/// A single input that broke a fuzzing property.
///
/// @param seed        the seed of the case that produced the input; replaying it reproduces the failure
/// @param iteration   the 0-based iteration within the run
/// @param input       the bytes that caused the failure
/// @param shrunkInput the smallest bytes that still fail the same way, or the input itself if it was not shrunk
/// @param failure     the exception thrown when processing the input
public record FuzzFailure(long seed, int iteration, byte[] input, byte[] shrunkInput, Throwable failure) {

    private static final HexFormat HEX = HexFormat.ofDelimiter(" ");

    public FuzzFailure {
        input = Objects.requireNonNull(input, "input").clone();
        shrunkInput = Objects.requireNonNull(shrunkInput, "shrunkInput").clone();
        Objects.requireNonNull(failure, "failure");
    }

    /// {@return a copy of the input}
    @Override
    public byte[] input() {
        return this.input.clone();
    }

    /// {@return a copy of the shrunk input}
    @Override
    public byte[] shrunkInput() {
        return this.shrunkInput.clone();
    }

    @Override
    public boolean equals(final Object other) {
        return other instanceof final FuzzFailure that
                && this.seed == that.seed
                && this.iteration == that.iteration
                && Arrays.equals(this.input, that.input)
                && Arrays.equals(this.shrunkInput, that.shrunkInput)
                && this.failure.equals(that.failure);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.seed, this.iteration, Arrays.hashCode(this.input), Arrays.hashCode(this.shrunkInput), this.failure);
    }

    @Override
    public String toString() {
        return "FuzzFailure[seed=%d, iteration=%d, input=%s, shrunkInput=%s, failure=%s]".formatted(
                this.seed, this.iteration, HEX.formatHex(this.input), HEX.formatHex(this.shrunkInput), this.failure);
    }

    /// {@return a multi-line, human-readable description of this failure}
    public String report() {
        final StringBuilder builder = new StringBuilder()
                .append("seed=%d, iteration=%d: %s".formatted(this.seed, this.iteration, this.failure))
                .append("\n    input  (%d bytes): %s".formatted(this.input.length, HEX.formatHex(this.input)));

        if (this.shrunkInput.length != this.input.length) {
            builder.append("\n    shrunk (%d bytes): %s"
                    .formatted(this.shrunkInput.length, HEX.formatHex(this.shrunkInput))
            );
        }

        return builder.toString();
    }

}
