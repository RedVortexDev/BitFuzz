package red.vortx.bitfuzz.schema.std;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import red.vortx.bitfuzz.buffer.ByteReader;
import red.vortx.bitfuzz.schema.api.Endianness;

import static org.assertj.core.api.Assertions.assertThat;
import static red.vortx.bitfuzz.schema.std.TestSchemas.assertRejects;
import static red.vortx.bitfuzz.schema.std.TestSchemas.assertRoundTrip;
import static red.vortx.bitfuzz.schema.std.TestSchemas.assertSizeOfMatches;
import static red.vortx.bitfuzz.schema.std.TestSchemas.bytes;
import static red.vortx.bitfuzz.schema.std.TestSchemas.decode;
import static red.vortx.bitfuzz.schema.std.TestSchemas.encode;

class VarIntTypeTest {

    private static void assertEncodes(final int value, final int... expected) {
        assertThat(encode(Types.VAR_INT, value)).containsExactly(bytes(expected));
        assertThat(decode(Types.VAR_INT, bytes(expected))).isEqualTo(value);
        assertThat(Types.VAR_INT.sizeOf(value)).isEqualTo(expected.length);
    }

    @Test
    void knownEncodings() {
        assertEncodes(0, 0x00);
        assertEncodes(1, 0x01);
        assertEncodes(127, 0x7F);
        assertEncodes(128, 0x80, 0x01);
        assertEncodes(300, 0xAC, 0x02);
        assertEncodes(16_383, 0xFF, 0x7F);
        assertEncodes(16_384, 0x80, 0x80, 0x01);
        assertEncodes(Integer.MAX_VALUE, 0xFF, 0xFF, 0xFF, 0xFF, 0x07);
        assertEncodes(-1, 0xFF, 0xFF, 0xFF, 0xFF, 0x0F);
        assertEncodes(Integer.MIN_VALUE, 0x80, 0x80, 0x80, 0x80, 0x08);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, -1, 127, 128, 255, 256, 65_535, 1 << 21, 1 << 28, Integer.MAX_VALUE, Integer.MIN_VALUE})
    void roundTripAndSize(final int value) {
        assertRoundTrip(Types.VAR_INT, value);
        assertSizeOfMatches(Types.VAR_INT, value);
    }

    @Test
    void ignoresSchemaEndianness() {
        assertThat(encode(Types.VAR_INT, Endianness.LITTLE_ENDIAN, 300)).containsExactly(bytes(0xAC, 0x02));
    }

    @Test
    void acceptsOverlongEncoding() {
        assertThat(decode(Types.VAR_INT, bytes(0x80, 0x00))).isEqualTo(0);
        assertThat(decode(Types.VAR_INT, bytes(0x81, 0x80, 0x00))).isEqualTo(1);
    }

    @Test
    void rejectsMoreThanFiveBytes() {
        assertRejects(Types.VAR_INT, 0x80, 0x80, 0x80, 0x80, 0x80, 0x00);
    }

    @Test
    void rejectsFifthByteOverflow() {
        assertRejects(Types.VAR_INT, 0xFF, 0xFF, 0xFF, 0xFF, 0x1F);
        assertRejects(Types.VAR_INT, 0xFF, 0xFF, 0xFF, 0xFF, 0x8F);
    }

    @Test
    void rejectsTruncated() {
        assertRejects(Types.VAR_INT, 0x80);
        assertRejects(Types.VAR_INT);
    }

    @Test
    void readsOnlyItsOwnBytes() {
        final ByteReader input = TestBuffers.reader(0xAC, 0x02, 0x7F);

        assertThat(Types.VAR_INT.read(input)).isEqualTo(300);
        assertThat(input.remaining()).isEqualTo(1);
    }

}
