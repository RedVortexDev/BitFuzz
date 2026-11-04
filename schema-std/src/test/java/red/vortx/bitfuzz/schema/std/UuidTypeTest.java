package red.vortx.bitfuzz.schema.std;

import org.junit.jupiter.api.Test;
import red.vortx.bitfuzz.schema.api.Endianness;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static red.vortx.bitfuzz.schema.std.TestSchemas.assertRejects;
import static red.vortx.bitfuzz.schema.std.TestSchemas.assertRoundTrip;
import static red.vortx.bitfuzz.schema.std.TestSchemas.assertSizeOfMatches;
import static red.vortx.bitfuzz.schema.std.TestSchemas.bytes;
import static red.vortx.bitfuzz.schema.std.TestSchemas.decode;
import static red.vortx.bitfuzz.schema.std.TestSchemas.encode;

class UuidTypeTest {

    private static final UUID SAMPLE = UUID.fromString("01234567-89ab-cdef-fedc-ba9876543210");
    private static final byte[] SAMPLE_BYTES = bytes(
            0x01, 0x23, 0x45, 0x67, 0x89, 0xAB, 0xCD, 0xEF,
            0xFE, 0xDC, 0xBA, 0x98, 0x76, 0x54, 0x32, 0x10);

    @Test
    void encodesInRfcByteOrder() {
        assertThat(encode(Types.UUID, SAMPLE)).containsExactly(SAMPLE_BYTES);
        assertThat(decode(Types.UUID, SAMPLE_BYTES)).isEqualTo(SAMPLE);
    }

    @Test
    void ignoresSchemaEndianness() {
        assertThat(encode(Types.UUID, Endianness.LITTLE_ENDIAN, SAMPLE)).containsExactly(SAMPLE_BYTES);
    }

    @Test
    void roundTripAndSize() {
        for (final UUID value : new UUID[]{SAMPLE, UUID.randomUUID(), new UUID(0, 0), new UUID(-1, -1)}) {
            assertRoundTrip(Types.UUID, value);
            assertSizeOfMatches(Types.UUID, value);
        }
        assertThat(Types.UUID.sizeOf(SAMPLE)).isEqualTo(16);
    }

    @Test
    void rejectsTruncated() {
        assertRejects(Types.UUID, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09);
    }

}
