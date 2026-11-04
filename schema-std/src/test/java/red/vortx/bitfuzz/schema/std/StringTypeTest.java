package red.vortx.bitfuzz.schema.std;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import red.vortx.bitfuzz.schema.api.Endianness;
import red.vortx.bitfuzz.schema.api.Schema;
import red.vortx.bitfuzz.schema.api.SchemaException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static red.vortx.bitfuzz.schema.std.TestBuffers.written;
import static red.vortx.bitfuzz.schema.std.TestSchemas.assertRejects;
import static red.vortx.bitfuzz.schema.std.TestSchemas.assertRoundTrip;
import static red.vortx.bitfuzz.schema.std.TestSchemas.assertSizeOfMatches;
import static red.vortx.bitfuzz.schema.std.TestSchemas.bytes;
import static red.vortx.bitfuzz.schema.std.TestSchemas.decode;
import static red.vortx.bitfuzz.schema.std.TestSchemas.encode;
import static red.vortx.bitfuzz.schema.std.TestSchemas.single;

class StringTypeTest {

    @Test
    void encodesByteLengthThenUtf8() {
        assertThat(encode(Types.STRING, "hi")).containsExactly(bytes(0, 0, 0, 2, 'h', 'i'));
    }

    @Test
    void emptyString() {
        assertThat(encode(Types.STRING, "")).containsExactly(bytes(0, 0, 0, 0));
        assertThat(decode(Types.STRING, bytes(0, 0, 0, 0))).isEqualTo("");
    }

    @Test
    void lengthCountsBytesNotCharacters() {
        assertThat(encode(Types.STRING, "é")).as("1 char, 2 bytes").containsExactly(bytes(0, 0, 0, 2, 0xC3, 0xA9));
        assertThat(encode(Types.STRING, "😀")).as("2 chars, 4 bytes")
                .containsExactly(bytes(0, 0, 0, 4, 0xF0, 0x9F, 0x98, 0x80));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "a", "hello world", "é", "日本語", "😀", "mixed é 日本 😀 text", "\u0000"})
    void roundTripAndSize(final String value) {
        assertRoundTrip(Types.STRING, value);
        assertSizeOfMatches(Types.STRING, value);
    }

    @Test
    void longString() {
        final String value = "x".repeat(100_000);

        assertRoundTrip(Types.STRING, value);
        assertSizeOfMatches(Types.STRING, value);
    }

    @Test
    void lengthFollowsSchemaEndianness() {
        assertThat(encode(Types.STRING, Endianness.LITTLE_ENDIAN, "hi")).containsExactly(bytes(2, 0, 0, 0, 'h', 'i'));
    }

    @Test
    void customLengthType() {
        assertThat(encode(Types.string(Types.VAR_INT), "hi")).containsExactly(bytes(2, 'h', 'i'));
        assertThat(encode(Types.string(Types.VAR_INT), "")).containsExactly(bytes(0));
        assertRoundTrip(Types.string(Types.VAR_INT), "日本語");
        assertSizeOfMatches(Types.string(Types.VAR_INT), "x".repeat(200));
    }

    @Test
    void rejectsNegativeLength() {
        assertRejects(Types.STRING, 0xFF, 0xFF, 0xFF, 0xFF);
    }

    @Test
    void rejectsLengthPastEnd() {
        assertRejects(Types.STRING, 0, 0, 0, 5, 'h', 'i');
        assertRejects(Types.STRING, 0x7F, 0xFF, 0xFF, 0xFF, 'h');
    }

    @Test
    void rejectsMalformedUtf8() {
        assertRejects(Types.STRING, 0, 0, 0, 1, 0xFF);
        assertRejects(Types.STRING, 0, 0, 0, 1, 0xC3);
        assertRejects(Types.STRING, 0, 0, 0, 2, 0xC0, 0x80);
    }

    @Test
    void rejectsUnpairedSurrogateOnWrite() {
        final Schema schema = single(Types.STRING);

        assertThatThrownBy(() -> written(out -> schema.write(out, List.of("bad \uD800 surrogate"))))
                .isInstanceOf(SchemaException.class);
    }

}
