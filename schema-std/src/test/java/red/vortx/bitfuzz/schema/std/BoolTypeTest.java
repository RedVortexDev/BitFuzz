package red.vortx.bitfuzz.schema.std;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static red.vortx.bitfuzz.schema.std.TestSchemas.assertRejects;
import static red.vortx.bitfuzz.schema.std.TestSchemas.assertRoundTrip;
import static red.vortx.bitfuzz.schema.std.TestSchemas.assertSizeOfMatches;
import static red.vortx.bitfuzz.schema.std.TestSchemas.bytes;
import static red.vortx.bitfuzz.schema.std.TestSchemas.decode;
import static red.vortx.bitfuzz.schema.std.TestSchemas.encode;

class BoolTypeTest {

    @Test
    void encodesAsOneByte() {
        assertThat(encode(Types.BOOL, true)).containsExactly(bytes(1));
        assertThat(encode(Types.BOOL, false)).containsExactly(bytes(0));
    }

    @Test
    void roundTripAndSize() {
        assertRoundTrip(Types.BOOL, true);
        assertRoundTrip(Types.BOOL, false);
        assertSizeOfMatches(Types.BOOL, true);
    }

    @Test
    void decodesOnlyZeroAndOne() {
        assertThat(decode(Types.BOOL, bytes(0))).isEqualTo(false);
        assertThat(decode(Types.BOOL, bytes(1))).isEqualTo(true);
    }

    @Test
    void rejectsOtherBytes() {
        assertRejects(Types.BOOL, 2);
        assertRejects(Types.BOOL, 0xFF);
    }

    @Test
    void rejectsTruncated() {
        assertRejects(Types.BOOL);
    }

}
