package red.vortx.bitfuzz.fuzz.api;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class FuzzFailureTest {

    private static final IllegalStateException BUG = new IllegalStateException("bug");

    @Test
    void copiesArraysOnConstruction() {
        final byte[] input = {1, 2, 3};
        final byte[] shrunk = {3};
        final FuzzFailure failure = new FuzzFailure(1, 0, input, shrunk, BUG);

        input[0] = 9;
        shrunk[0] = 9;

        assertThat(failure.input()).containsExactly(1, 2, 3);
        assertThat(failure.shrunkInput()).containsExactly(3);
    }

    @Test
    void accessorsReturnCopies() {
        final FuzzFailure failure = new FuzzFailure(1, 0, new byte[]{1, 2}, new byte[]{2}, BUG);

        failure.input()[0] = 9;
        failure.shrunkInput()[0] = 9;

        assertThat(failure.input()).containsExactly(1, 2);
        assertThat(failure.shrunkInput()).containsExactly(2);
    }

    @Test
    void equalityComparesContents() {
        final FuzzFailure first = new FuzzFailure(1, 0, new byte[]{1, 2}, new byte[]{2}, BUG);
        final FuzzFailure second = new FuzzFailure(1, 0, new byte[]{1, 2}, new byte[]{2}, BUG);

        assertThat(first).isEqualTo(second).hasSameHashCodeAs(second);
        assertThat(first).isNotEqualTo(new FuzzFailure(1, 0, new byte[]{1, 3}, new byte[]{2}, BUG));
    }

    @Test
    void toStringShowsHex() {
        assertThat(new FuzzFailure(1, 0, new byte[]{(byte) 0xAB}, new byte[]{(byte) 0xAB}, BUG).toString())
                .contains("input=ab")
                .doesNotContain("[B@");
    }

    @Test
    void rejectsNulls() {
        assertThatNullPointerException()
                .isThrownBy(() -> new FuzzFailure(1, 0, null, new byte[0], BUG))
                .withMessage("input");
        assertThatNullPointerException()
                .isThrownBy(() -> new FuzzFailure(1, 0, new byte[0], null, BUG))
                .withMessage("shrunkInput");
        assertThatNullPointerException()
                .isThrownBy(() -> new FuzzFailure(1, 0, new byte[0], new byte[0], null))
                .withMessage("failure");
    }

}
