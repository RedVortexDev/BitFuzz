// This file is part of BitFuzz.
// SPDX-FileCopyrightText: 2026 RedVortexDev
// SPDX-License-Identifier: Apache-2.0

package red.vortx.bitfuzz.schema.std;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import red.vortx.bitfuzz.schema.api.Endianness;
import red.vortx.bitfuzz.schema.api.Schema;
import red.vortx.bitfuzz.schema.api.SchemaException;
import red.vortx.bitfuzz.schema.std.type.Types;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static red.vortx.bitfuzz.schema.std.TestBuffers.written;
import static red.vortx.bitfuzz.schema.std.TestSchemas.*;

class StringTypeTest {

    @Test
    void encodesByteLengthThenUtf8() {
        assertThat(encode(Types.STRING, "hi")).containsExactly(bytes(2, 'h', 'i'));
    }

    @Test
    void emptyString() {
        assertThat(encode(Types.STRING, "")).containsExactly(bytes(0));
        assertThat(decode(Types.STRING, bytes(0))).isEqualTo("");
    }

    @Test
    void lengthCountsBytesNotCharacters() {
        assertThat(encode(Types.STRING, "é")).as("1 char, 2 bytes").containsExactly(bytes(2, 0xC3, 0xA9));
        assertThat(encode(Types.STRING, "😀")).as("2 chars, 4 bytes")
                .containsExactly(bytes(4, 0xF0, 0x9F, 0x98, 0x80));
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
    void intLengthFollowsSchemaEndianness() {
        assertThat(encode(Types.stringOf(Types.INT), Endianness.LITTLE_ENDIAN, "hi")).containsExactly(bytes(2, 0, 0, 0, 'h', 'i'));
    }

    @Test
    void customLengthType() {
        assertThat(encode(Types.stringOf(Types.INT), "hi")).containsExactly(bytes(0, 0, 0, 2, 'h', 'i'));
        assertThat(encode(Types.stringOf(Types.INT), "")).containsExactly(bytes(0, 0, 0, 0));
        assertRoundTrip(Types.stringOf(Types.INT), "日本語");
        assertSizeOfMatches(Types.stringOf(Types.INT), "x".repeat(200));
    }

    @Test
    void rejectsNegativeLength() {
        assertRejects(Types.STRING, 0xFF, 0xFF, 0xFF, 0xFF, 0x0F);
    }

    @Test
    void rejectsLengthPastEnd() {
        assertRejects(Types.STRING, 5, 'h', 'i');
        assertRejects(Types.STRING, 0xFF, 0xFF, 0xFF, 0xFF, 0x07, 'h');
    }

    @Test
    void rejectsMalformedUtf8() {
        assertRejects(Types.STRING, 1, 0xFF);
        assertRejects(Types.STRING, 1, 0xC3);
        assertRejects(Types.STRING, 2, 0xC0, 0x80);
    }

    @Test
    void rejectsUnpairedSurrogateOnWrite() {
        final Schema schema = single(Types.STRING);

        assertThatThrownBy(() -> written(out -> schema.write(out, List.of("bad \uD800 surrogate"))))
                .isInstanceOf(SchemaException.class);
    }

}
