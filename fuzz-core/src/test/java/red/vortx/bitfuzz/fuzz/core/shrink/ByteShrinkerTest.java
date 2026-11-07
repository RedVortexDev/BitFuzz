// This file is part of BitFuzz.
// SPDX-FileCopyrightText: 2026 RedVortexDev
// SPDX-License-Identifier: Apache-2.0

package red.vortx.bitfuzz.fuzz.core.shrink;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.Arrays;
import java.util.HexFormat;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(10)
class ByteShrinkerTest {

    private static final ByteShrinker SHRINKER = new ByteShrinker();

    private static byte[] hex(final String hex) {
        return HexFormat.ofDelimiter(" ").parseHex(hex);
    }

    private static boolean containsFfFf(final byte[] bytes) {
        for (int i = 0; i + 1 < bytes.length; i++) {
            if (bytes[i] == (byte) 0xFF && bytes[i + 1] == (byte) 0xFF) {
                return true;
            }
        }
        return false;
    }

    @Test
    void removesEverythingThatDoesNotMatter() {
        final byte[] input = hex("01 02 03 ff ff 04 05 06 07 08");

        assertThat(SHRINKER.shrink(input, ByteShrinkerTest::containsFfFf)).containsExactly(hex("ff ff"));
    }

    @Test
    void zeroesBytesThatOnlyNeedToExist() {
        final Predicate<byte[]> firstIsSevenAndAtLeastThreeBytes = bytes -> bytes.length >= 3 && bytes[0] == 7;

        assertThat(SHRINKER.shrink(hex("07 aa bb cc dd"), firstIsSevenAndAtLeastThreeBytes))
                .containsExactly(hex("07 00 00"));
    }

    @Test
    void negativeLengthExampleShrinksToFiveBytes() {
        final Predicate<byte[]> negativeLengthAfterVarint = bytes -> bytes.length >= 5
                && (bytes[0] & 0x80) == 0
                && bytes[1] < 0;

        assertThat(SHRINKER.shrink(hex("01 ff ff ff ff 68 65 6c 6c 6f"), negativeLengthAfterVarint))
                .containsExactly(hex("00 ff 00 00 00"));
    }

    @Test
    void returnsCopyWhenNothingCanBeRemoved() {
        final byte[] input = hex("01 02 03");
        final byte[] shrunk = SHRINKER.shrink(input, bytes -> Arrays.equals(bytes, hex("01 02 03")));

        assertThat(shrunk).containsExactly(input).isNotSameAs(input);
    }

    @Test
    void neverModifiesInput() {
        final byte[] input = hex("01 02 03 ff ff 04");

        SHRINKER.shrink(input, ByteShrinkerTest::containsFfFf);

        assertThat(input).containsExactly(hex("01 02 03 ff ff 04"));
    }

    @Test
    void handlesEmptyInput() {
        assertThat(SHRINKER.shrink(new byte[0], bytes -> true)).isEmpty();
    }

    @Test
    void canShrinkToNothing() {
        assertThat(SHRINKER.shrink(hex("01 02 03 04"), bytes -> true)).isEmpty();
    }

    @Test
    void respectsAttemptLimit() {
        final AtomicInteger calls = new AtomicInteger();
        final byte[] input = new byte[10_000];
        Arrays.fill(input, (byte) 1);

        new ByteShrinker(50).shrink(input, bytes -> {
            calls.incrementAndGet();
            return bytes.length > 9_000;
        });

        assertThat(calls).hasValueLessThanOrEqualTo(50);
    }

    @Test
    void resultAlwaysStillFails() {
        final Predicate<byte[]> sumIsOdd = bytes -> {
            int sum = 0;
            for (final byte b : bytes) {
                sum += b;
            }
            return (sum & 1) == 1;
        };

        final byte[] shrunk = SHRINKER.shrink(hex("02 04 06 07 08 0a"), sumIsOdd);

        assertThat(sumIsOdd).accepts(shrunk);
        assertThat(shrunk).hasSize(1);
    }

}
