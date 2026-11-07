// This file is part of BitFuzz.
// SPDX-FileCopyrightText: 2026 RedVortexDev
// SPDX-License-Identifier: Apache-2.0

package red.vortx.bitfuzz.schema.std;

import org.junit.jupiter.api.Test;
import red.vortx.bitfuzz.buffer.ByteReader;
import red.vortx.bitfuzz.schema.api.Endianness;
import red.vortx.bitfuzz.schema.api.Schema;
import red.vortx.bitfuzz.schema.api.SchemaException;
import red.vortx.bitfuzz.schema.api.field.type.TypeSpec;
import red.vortx.bitfuzz.schema.std.type.Types;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static red.vortx.bitfuzz.schema.std.TestBuffers.reader;
import static red.vortx.bitfuzz.schema.std.TestSchemas.*;

class MapTypeTest {

    private static final TypeSpec<Map<Byte, Short>> BYTE_TO_SHORT = Types.map(Types.BYTE, Types.SHORT);

    private static <K, V> Map<K, V> ordered(final Object... keysAndValues) {
        final Map<K, V> map = new LinkedHashMap<>();
        for (int i = 0; i < keysAndValues.length; i += 2) {
            //noinspection unchecked
            map.put((K) keysAndValues[i], (V) keysAndValues[i + 1]);
        }
        return map;
    }

    @Test
    void encodesCountThenEntries() {
        final Map<Byte, Short> map = ordered((byte) 1, (short) 0x0203, (byte) 4, (short) 0x0506);

        assertThat(encode(BYTE_TO_SHORT, map)).containsExactly(bytes(2, 1, 2, 3, 4, 5, 6));
    }

    @Test
    void emptyMap() {
        assertThat(encode(BYTE_TO_SHORT, Map.of())).containsExactly(bytes(0));
        assertThat(decode(BYTE_TO_SHORT, bytes(0))).isEqualTo(Map.of());
    }

    @Test
    void readKeepsByteOrder() {
        final Object read = decode(BYTE_TO_SHORT, bytes(3, 9, 0, 1, 1, 0, 2, 5, 0, 3));

        assertThat(List.copyOf(((Map<?, ?>) read).keySet())).isEqualTo(List.of((byte) 9, (byte) 1, (byte) 5));
    }

    @Test
    void keysAndValuesFollowSchemaEndianness() {
        final Map<Byte, Short> map = ordered((byte) 1, (short) 0x0203);

        assertThat(encode(BYTE_TO_SHORT, Endianness.LITTLE_ENDIAN, map)).containsExactly(bytes(1, 1, 3, 2));
    }

    @Test
    void roundTripAndSize() {
        final Map<String, List<Integer>> map = ordered("a", List.of(1, 2), "", List.of(), "日本", List.of(-1));
        final TypeSpec<Map<String, List<Integer>>> type = Types.map(Types.STRING, Types.list(Types.INT));

        assertRoundTrip(type, map);
        assertSizeOfMatches(type, map);
    }

    @Test
    void customLengthType() {
        final TypeSpec<Map<Byte, Byte>> type = Types.map(Types.BYTE, Types.BYTE, Types.INT);

        assertThat(encode(type, ordered((byte) 1, (byte) 2))).containsExactly(bytes(0, 0, 0, 1, 1, 2));
        assertRoundTrip(type, ordered((byte) 1, (byte) 2, (byte) 3, (byte) 4));
    }

    @Test
    void rejectsDuplicateKeys() {
        assertRejects(BYTE_TO_SHORT, 2, 1, 0, 1, 1, 0, 2);
    }

    @Test
    void rejectsNegativeLength() {
        assertRejects(BYTE_TO_SHORT, 0xFF, 0xFF, 0xFF, 0xFF, 0x0F);
    }

    @Test
    void rejectsHugeLength() {
        assertRejects(BYTE_TO_SHORT, 0xFF, 0xFF, 0xFF, 0xFF, 0x07, 1, 0, 1);
    }

    @Test
    void rejectsTruncatedEntry() {
        assertRejects(BYTE_TO_SHORT, 1, 1, 0);
    }

    @Test
    void rejectsWrongKeyTypeOnWrite() {
        final Schema schema = single(BYTE_TO_SHORT);

        assertThatThrownBy(() -> TestBuffers.written(out -> schema.write(out, List.of(Map.of("no", (short) 1)))))
                .isInstanceOf(SchemaException.class);
    }

    @Test
    void readsOnlyItsOwnBytes() {
        final Schema schema = single(BYTE_TO_SHORT);
        final ByteReader input = reader(1, 1, 0, 2, 0xAA);

        schema.read(input);

        assertThat(input.remaining()).isEqualTo(1);
    }

}
