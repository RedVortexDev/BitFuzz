package red.vortx.bitfuzz.fuzz.api;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FuzzResultTest {

    private static FuzzFailure failure(final int iteration, final byte[] input, final byte[] shrunk) {
        return new FuzzFailure(100 + iteration, iteration, input, shrunk, new IllegalStateException("bug " + iteration));
    }

    @Test
    void cleanReport() {
        final FuzzResult result = new FuzzResult(500, List.of());

        assertThat(result.isClean()).isTrue();
        assertThat(result.report()).isEqualTo("Fuzzing finished after 500 iterations, no failures found.");
    }

    @Test
    void failureReportShowsHexAndShrunkInput() {
        final String report = failure(3, new byte[]{1, (byte) 0xFF, 2}, new byte[]{(byte) 0xFF}).report();

        assertThat(report)
                .contains("seed=103")
                .contains("iteration=3")
                .contains("bug 3")
                .contains("input  (3 bytes): 01 ff 02")
                .contains("shrunk (1 bytes): ff");
    }

    @Test
    void unshrunkFailureOmitsShrunkLine() {
        final byte[] input = {1, 2};

        assertThat(failure(0, input, input).report()).doesNotContain("shrunk");
    }

    @Test
    void reportShowsOnlyFirstFive() {
        final List<FuzzFailure> failures = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            failures.add(failure(i, new byte[]{(byte) i}, new byte[]{(byte) i}));
        }

        final String report = new FuzzResult(1_000, failures).report();

        assertThat(report).contains("found 8 failures (showing first 5)");
        assertThat(report).contains("bug 4").doesNotContain("bug 5");
    }

    @Test
    void failuresAreCopied() {
        final List<FuzzFailure> failures = new ArrayList<>();
        final FuzzResult result = new FuzzResult(1, failures);

        failures.add(failure(0, new byte[0], new byte[0]));

        assertThat(result.failures()).isEmpty();
    }

}
