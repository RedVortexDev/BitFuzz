package red.vortx.bitfuzz.schema.std;

import org.junit.jupiter.api.Test;
import red.vortx.bitfuzz.schema.api.Endianness;
import red.vortx.bitfuzz.schema.api.Schema;
import red.vortx.bitfuzz.schema.api.SchemaException;
import red.vortx.bitfuzz.schema.api.field.type.Type;
import red.vortx.bitfuzz.schema.core.SchemaBuilderImpl;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static red.vortx.bitfuzz.schema.std.TestBuffers.reader;
import static red.vortx.bitfuzz.schema.std.TestBuffers.written;

class ListTypeTest {

    private static final ListType<Short> SHORTS = new ListType<>(Types.SHORT_BE, Types.INT_BE);

    private static Schema listOfInts(final Endianness endianness) {
        return new SchemaBuilderImpl()
                .defaults(defaults -> defaults.endianness(endianness).lengthType(Types.INT))
                .field(Types.list(Types.INT), "list")
                .build();
    }

    @Test
    void encodesLengthThenElements() {
        final byte[] bytes = written(out -> SHORTS.write(out, List.of((short) 0x0102, (short) 0x0304)));

        assertThat(bytes).containsExactly(0, 0, 0, 2, 1, 2, 3, 4);
    }

    @Test
    void roundTrip() {
        final List<Short> values = List.of((short) 1, (short) -1, Short.MAX_VALUE, Short.MIN_VALUE);

        final byte[] bytes = written(out -> SHORTS.write(out, values));

        assertThat(SHORTS.read(reader(bytes))).containsExactlyElementsOf(values);
    }

    @Test
    void emptyList() {
        final byte[] bytes = written(out -> SHORTS.write(out, List.of()));

        assertThat(bytes).containsExactly(0, 0, 0, 0);
        assertThat(SHORTS.read(reader(bytes))).isEmpty();
    }

    @Test
    void sizeOfMatchesWrittenBytes() {
        final List<Short> values = List.of((short) 1, (short) 2, (short) 3);

        assertThat(SHORTS.sizeOf(values)).isEqualTo(written(out -> SHORTS.write(out, values)).length);
        assertThat(SHORTS.sizeOf(List.of())).isEqualTo(4);
    }

    @Test
    void nestedLists() {
        final ListType<List<Short>> nested = new ListType<>(SHORTS, Types.INT_BE);
        final List<List<Short>> values = List.of(List.of((short) 1), List.of(), List.of((short) 2, (short) 3));

        final byte[] bytes = written(out -> nested.write(out, values));

        assertThat(nested.read(reader(bytes))).isEqualTo(values);
    }

    @Test
    void javaTypeIsList() {
        assertThat(SHORTS.javaType()).isEqualTo(List.class);
    }

    @Test
    void bigEndianSchema() {
        final Schema schema = listOfInts(Endianness.BIG_ENDIAN);

        final byte[] bytes = written(out -> schema.write(out, List.of(List.of(1))));

        assertThat(bytes).containsExactly(0, 0, 0, 1, 0, 0, 0, 1);
    }

    @Test
    void littleEndianSchemaAppliesToElementsAndLength() {
        final Schema schema = listOfInts(Endianness.LITTLE_ENDIAN);

        final byte[] bytes = written(out -> schema.write(out, List.of(List.of(1))));

        assertThat(bytes).containsExactly(1, 0, 0, 0, 1, 0, 0, 0);
        assertThat(schema.read(reader(bytes))).containsExactly(List.of(1));
    }

    @Test
    void defaultLengthTypeFollowsSchemaEndianness() {
        final Schema schema = new SchemaBuilderImpl()
                .defaults(defaults -> defaults.endianness(Endianness.LITTLE_ENDIAN))
                .field(Types.list(Types.INT), "list")
                .build();

        final byte[] bytes = written(out -> schema.write(out, List.of(List.of(1))));

        assertThat(bytes).containsExactly(1, 0, 0, 0, 1, 0, 0, 0);
    }

    @Test
    void wrongElementTypeIsSchemaException() {
        final Schema schema = listOfInts(Endianness.BIG_ENDIAN);

        assertThatThrownBy(() -> written(out -> schema.write(out, List.of(List.of("nope")))))
                .isInstanceOf(SchemaException.class);
    }

    @Test
    void truncatedElementsIsSchemaException() {
        final Schema schema = listOfInts(Endianness.BIG_ENDIAN);

        assertThatThrownBy(() -> schema.read(reader(0, 0, 0, 2, 0, 0, 0, 1)))
                .isInstanceOf(SchemaException.class);
    }

    @Test
    void negativeLengthIsSchemaException() {
        final Schema schema = listOfInts(Endianness.BIG_ENDIAN);

        assertThatThrownBy(() -> schema.read(reader(0xFF, 0xFF, 0xFF, 0xFF)))
                .isInstanceOf(SchemaException.class);
    }

    @Test
    void hugeLengthWithFewBytesIsSchemaException() {
        final Schema schema = listOfInts(Endianness.BIG_ENDIAN);

        assertThatThrownBy(() -> schema.read(reader(0x7F, 0xFF, 0xFF, 0xFF, 0, 0, 0, 1)))
                .isInstanceOf(SchemaException.class);
    }

    @Test
    void listSpecResolvesElementType() {
        final Schema schema = new SchemaBuilderImpl()
                .defaults(defaults -> defaults.endianness(Endianness.LITTLE_ENDIAN))
                .field(Types.list(Types.SHORT), "list")
                .build();

        final Type<?> type = schema.fields().getFirst().type();

        assertThat(type).isInstanceOf(ListType.class);
        assertThat(((ListType<?>) type).elementType()).isSameAs(Types.SHORT_LE);
    }

}
