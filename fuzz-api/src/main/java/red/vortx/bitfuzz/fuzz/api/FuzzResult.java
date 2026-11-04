package red.vortx.bitfuzz.fuzz.api;

import java.util.List;

/// The outcome of a fuzzing run.
///
/// @param iterations the number of inputs tried
/// @param failures   the inputs that broke the property
public record FuzzResult(int iterations, List<FuzzFailure> failures) {

    private static final int MAX_FAILURES_TO_REPORT = 5;

    public FuzzResult(final int iterations, final List<FuzzFailure> failures) {
        this.iterations = iterations;
        this.failures = List.copyOf(failures);
    }

    /// {@return whether no input broke the property}
    public boolean isClean() {
        return this.failures.isEmpty();
    }

    /// {@return a human-readable summary listing the first few failures}
    public String report() {
        final StringBuilder builder = new StringBuilder();
        builder.append("Fuzzing finished after %d iterations".formatted(this.iterations));

        if (this.isClean()) {
            builder.append(", no failures found.");
            return builder.toString();
        }

        builder.append(", found %d failures".formatted(this.failures.size()));
        if (this.failures.size() > MAX_FAILURES_TO_REPORT) {
            builder.append(" (showing first %d)".formatted(MAX_FAILURES_TO_REPORT));
        }

        for (final FuzzFailure failure : this.failures.subList(0, Math.min(this.failures.size(), MAX_FAILURES_TO_REPORT))) {
            builder.append("\n- ").append(failure.report());
        }

        return builder.toString();
    }

}
