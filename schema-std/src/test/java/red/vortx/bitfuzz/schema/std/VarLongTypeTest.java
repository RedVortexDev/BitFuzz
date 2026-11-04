package red.vortx.bitfuzz.schema.std;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static red.vortx.bitfuzz.schema.std.TestSchemas.assertRejects;
import static red.vortx.bitfuzz.schema.std.TestSchemas.assertRoundTrip;
import static red.vortx.bitfuzz.schema.std.TestSchemas.assertSizeOfMatches;
import static red.vortx.bitfuzz.schema.std.TestSchemas.bytes;
import static red.vortx.bitfuzz.schema.std.TestSchemas.decode;
import static red.vortx.bitfuzz.schema.std.TestSchemas.encode;

class VarLongTypeTest {

    private static void assertEncodes(final long value, final int... expected) {
        assertThat(encode(Types.VAR_LONG, value)).containsExactly(bytes(expected));
        assertThat(decode(Types.VAR_LONG, bytes(expected))).isEqualTo(value);
        assertThat(Types.VAR_LONG.sizeOf(value)).isEqualTo(expected.length);
    }

    @Test
    void knownEncodings() {
        assertEncodes(0L, 0x00);
        assertEncodes(1L, 0x01);
        assertEncodes(300L, 0xAC, 0x02);
        assertEncodes(Integer.MAX_VALUE, 0xFF, 0xFF, 0xFF, 0xFF, 0x07);
        assertEncodes(1L << 35, 0x80, 0x80, 0x80, 0x80, 0x80, 0x01);
        assertEncodes(Long.MAX_VALUE, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0x7F);
        assertEncodes(-1L, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0x01);
        assertEncodes(Long.MIN_VALUE, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x01);
    }

    @ParameterizedTest
    @ValueSource(longs = {0, 1, -1, 127, 128, 1L << 31, 1L << 32, 1L << 56, 1L << 62, Long.MAX_VALUE, Long.MIN_VALUE})
    void roundTripAndSize(final long value) {
        assertRoundTrip(Types.VAR_LONG, value);
        assertSizeOfMatches(Types.VAR_LONG, value);
    }

    @Test
    void rejectsMoreThanTenBytes() {
        assertRejects(Types.VAR_LONG, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x00);
    }

    @Test
    void rejectsTenthByteOverflow() {
        assertRejects(Types.VAR_LONG, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0x02);
        assertRejects(Types.VAR_LONG, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0x81);
    }

    @Test
    void rejectsTruncated() {
        assertRejects(Types.VAR_LONG, 0xFF, 0xFF);
    }

}
