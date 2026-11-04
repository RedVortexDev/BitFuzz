package red.vortx.bitfuzz.fuzz.api;

import java.util.HexFormat;

/// A single input that broke a fuzzing property.
///
/// @param seed        the seed of the case that produced the input; replaying it reproduces the failure
/// @param iteration   the 0-based iteration within the run
/// @param input       the bytes that caused the failure
/// @param shrunkInput the smallest bytes that still fail the same way, or the input itself if it was not shrunk
/// @param failure     the exception thrown when processing the input
public record FuzzFailure(long seed, int iteration, byte[] input, byte[] shrunkInput, Throwable failure) {

    private static final HexFormat HEX = HexFormat.ofDelimiter(" ");

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
