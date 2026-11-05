package red.vortx.bitfuzz.schema.std.record;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import red.vortx.bitfuzz.fuzz.api.FuzzResult;
import red.vortx.bitfuzz.fuzz.core.Fuzz;
import red.vortx.bitfuzz.schema.api.Endianness;
import red.vortx.bitfuzz.schema.api.Schema;
import red.vortx.bitfuzz.schema.api.SchemaBuilder;
import red.vortx.bitfuzz.schema.api.SchemaException;
import red.vortx.bitfuzz.schema.std.Types;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static red.vortx.bitfuzz.schema.std.TestBuffers.reader;
import static red.vortx.bitfuzz.schema.std.TestBuffers.written;

class RecordTypeTest {

    record Point(int x, int y) {
    }

    record Player(
            @VarInt int id,
            String name,
            List<@VarInt Integer> scores,
            Optional<UUID> guild,
            Map<String, @VarLong Long> stats,
            boolean online,
            Point spawn
    ) {
    }

    record Positive(int value) {

        Positive {
            if (value <= 0) {
                throw new IllegalArgumentException("Value must be positive: %d".formatted(value));
            }
        }

    }

    record Unsupported(StringBuilder text) {
    }

    record Node(int value, List<Node> children) {
    }

    private static Schema schemaOf(final Class<? extends Record> recordClass) {
        return SchemaBuilder.create().field(Types.record(recordClass), "value").build();
    }

    private static Object roundTrip(final Schema schema, final Object value) {
        return schema.read(reader(written(out -> schema.write(out, List.of(value))))).getFirst();
    }

    @Test
    void writesComponentsInOrderWithNoFraming() {
        final Schema schema = schemaOf(Point.class);

        assertThat(written(out -> schema.write(out, List.of(new Point(1, 2))))).containsExactly(0, 0, 0, 1, 0, 0, 0, 2);
    }

    @Test
    void roundTripsEveryKindOfComponent() {
        final Player player = new Player(300, "steve", List.of(1, 128), Optional.of(UUID.randomUUID()), Map.of("kills", 5L), true, new Point(-1, 64));

        assertThat(roundTrip(schemaOf(Player.class), player)).isEqualTo(player);
    }

    @Test
    void wireTypeAnnotationsChangeTheEncoding() {
        final Player player = new Player(300, "", List.of(), Optional.empty(), Map.of(), false, new Point(0, 0));

        final byte[] bytes = written(out -> schemaOf(Player.class).write(out, List.of(player)));

        assertThat(bytes[0]).as("id 300 as a 2 byte varint").isEqualTo((byte) 0xAC);
        assertThat(bytes[1]).isEqualTo((byte) 0x02);
    }

    @Test
    void followsSchemaEndianness() {
        final Schema schema = SchemaBuilder.create()
                .defaults(defaults -> defaults.endianness(Endianness.LITTLE_ENDIAN))
                .field(Types.record(Point.class), "point")
                .build();

        assertThat(written(out -> schema.write(out, List.of(new Point(1, 2))))).containsExactly(1, 0, 0, 0, 2, 0, 0, 0);
    }

    @Test
    void compactConstructorValidationRejectsDecodedData() {
        final Schema schema = schemaOf(Positive.class);

        assertThat(roundTrip(schema, new Positive(5))).isEqualTo(new Positive(5));
        assertThatThrownBy(() -> schema.read(reader(0, 0, 0, 0)))
                .isInstanceOf(SchemaException.class)
                .hasRootCauseInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void nullComponentIsRejectedOnWrite() {
        final Schema schema = schemaOf(Player.class);
        final Player player = new Player(1, null, List.of(), Optional.empty(), Map.of(), false, new Point(0, 0));

        assertThatThrownBy(() -> written(out -> schema.write(out, List.of(player))))
                .isInstanceOf(SchemaException.class)
                .cause()
                .isInstanceOf(SchemaException.class)
                .hasMessageContaining("'name'");
    }

    @Test
    void unsupportedComponentFailsAtBuildWithAHint() {
        assertThatThrownBy(() -> schemaOf(Unsupported.class))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("StringBuilder")
                .hasMessageContaining("text")
                .hasMessageContaining("@WireType");
    }

    @Test
    void recursiveRecordsFailAtBuild() {
        assertThatThrownBy(() -> schemaOf(Node.class))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Recursive");
    }

    @Test
    @Timeout(60)
    void mappedRecordsSurviveFuzzing() {
        final Schema schema = schemaOf(Player.class);

        final FuzzResult roundTrip = Fuzz.roundTrip(schema, 5_000, 42);
        final FuzzResult mutated = Fuzz.mutate(schema, 20_000, 42);

        assertThat(roundTrip.isClean()).as(roundTrip.report()).isTrue();
        assertThat(mutated.isClean()).as(mutated.report()).isTrue();
    }

    @Test
    @Timeout(60)
    void fuzzingRespectsRecordValidation() {
        final Schema schema = schemaOf(Positive.class);

        final FuzzResult roundTrip = Fuzz.roundTrip(schema, 2_000, 42);

        assertThat(roundTrip.isClean()).as(roundTrip.report()).isTrue();
    }

}
