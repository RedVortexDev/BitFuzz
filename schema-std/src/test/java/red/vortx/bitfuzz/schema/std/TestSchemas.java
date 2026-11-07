package red.vortx.bitfuzz.schema.std;

import red.vortx.bitfuzz.schema.api.Endianness;
import red.vortx.bitfuzz.schema.api.Schema;
import red.vortx.bitfuzz.schema.api.SchemaBuilder;
import red.vortx.bitfuzz.schema.api.SchemaException;
import red.vortx.bitfuzz.schema.api.field.type.Type;
import red.vortx.bitfuzz.schema.api.field.type.TypeSpec;
import red.vortx.bitfuzz.schema.api.key.SchemaKeys;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static red.vortx.bitfuzz.schema.std.TestBuffers.reader;
import static red.vortx.bitfuzz.schema.std.TestBuffers.written;

enum TestSchemas {
    ;

    static Schema single(final TypeSpec<?> type, final Endianness endianness) {
        return SchemaBuilder.create()
                .defaults(defaults -> defaults.set(SchemaKeys.ENDIANNESS, endianness))
                .field(type, "value")
                .build();
    }

    static Schema single(final TypeSpec<?> type) {
        return single(type, Endianness.BIG_ENDIAN);
    }

    static <T> Type<T> resolve(final TypeSpec<T> type, final Endianness endianness) {
        //noinspection unchecked
        return (Type<T>) single(type, endianness).fields().getFirst().type();
    }

    static byte[] encode(final TypeSpec<?> type, final Object value) {
        return encode(type, Endianness.BIG_ENDIAN, value);
    }

    static byte[] encode(final TypeSpec<?> type, final Endianness endianness, final Object value) {
        final Schema schema = single(type, endianness);
        return written(out -> schema.write(out, List.of(value)));
    }

    static Object decode(final TypeSpec<?> type, final byte[] bytes) {
        return decode(type, Endianness.BIG_ENDIAN, bytes);
    }

    static Object decode(final TypeSpec<?> type, final Endianness endianness, final byte[] bytes) {
        return single(type, endianness).read(reader(bytes)).getFirst();
    }

    static void assertRoundTrip(final TypeSpec<?> type, final Object value) {
        for (final Endianness endianness : Endianness.values()) {
            final byte[] bytes = encode(type, endianness, value);
            assertThat(decode(type, endianness, bytes)).isEqualTo(value);
        }
    }

    static <T> void assertSizeOfMatches(final TypeSpec<T> type, final T value) {
        for (final Endianness endianness : Endianness.values()) {
            final Type<T> resolved = resolve(type, endianness);
            assertThat(resolved.sizeOf(value)).isEqualTo(encode(type, endianness, value).length);
        }
    }

    static void assertRejects(final TypeSpec<?> type, final int... bytes) {
        final Schema schema = single(type);
        assertThatThrownBy(() -> schema.read(reader(bytes))).isInstanceOf(SchemaException.class);
    }

    static byte[] bytes(final int... values) {
        final byte[] array = new byte[values.length];
        for (int i = 0; i < values.length; i++) {
            array[i] = (byte) values[i];
        }
        return array;
    }

}
