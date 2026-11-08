// This file is part of BitFuzz.
// SPDX-FileCopyrightText: 2026 RedVortexDev
// SPDX-License-Identifier: Apache-2.0

package red.vortx.bitfuzz.schema.std.record;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import red.vortx.bitfuzz.fuzz.api.FuzzResult;
import red.vortx.bitfuzz.fuzz.core.Fuzz;
import red.vortx.bitfuzz.schema.api.Endianness;
import red.vortx.bitfuzz.schema.api.Schema;
import red.vortx.bitfuzz.schema.api.SchemaBuilder;
import red.vortx.bitfuzz.schema.api.SchemaException;
import red.vortx.bitfuzz.schema.api.annotation.WireType;
import red.vortx.bitfuzz.schema.api.field.type.TypeSpec;
import red.vortx.bitfuzz.schema.api.key.SchemaKeys;
import red.vortx.bitfuzz.schema.std.key.SchemaStandardKeys;
import red.vortx.bitfuzz.schema.std.type.Types;
import red.vortx.bitfuzz.schema.std.type.enumeration.EnumEncoding;
import red.vortx.bitfuzz.schema.std.type.record.RecordTypes;
import red.vortx.bitfuzz.schema.std.type.record.VarInt;
import red.vortx.bitfuzz.schema.std.type.record.VarLong;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

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

    record Leaderboard(Point best, List<Integer> scores) {

    }

    record Mixed(int small, @Fixed int big) {

    }

    record Painted(Endianness order, List<Endianness> history) {

    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @WireType(Fixed.Spec.class)
    @interface Fixed {

        final class Spec implements Supplier<TypeSpec<?>> {

            @Override
            public TypeSpec<?> get() {
                return Types.INT;
            }

        }

    }

    private static Schema schemaOf(final Class<? extends Record> recordClass) {
        return SchemaBuilder.create().field(Types.recordOf(recordClass), "value").build();
    }

    private static Schema varIntSchemaOf(final Class<? extends Record> recordClass) {
        return SchemaBuilder.create()
                .defaults(defaults -> defaults.update(SchemaStandardKeys.RECORD_TYPES, types -> types.with(Integer.class, Types.VAR_INT)))
                .field(Types.recordOf(recordClass), "value")
                .build();
    }

    @Test
    void enumComponentsFollowSchemaEncoding() {
        final Painted painted = new Painted(Endianness.LITTLE_ENDIAN, List.of(Endianness.BIG_ENDIAN));
        final Schema named = SchemaBuilder.create()
                .defaults(defaults -> defaults.set(SchemaStandardKeys.ENUM_ENCODING, EnumEncoding.NAME))
                .field(Types.recordOf(Painted.class), "value")
                .build();

        assertThat(written(out -> schemaOf(Painted.class).write(out, List.of(painted))))
                .as("ordinals as varints by default")
                .containsExactly(Endianness.LITTLE_ENDIAN.ordinal(), 1, Endianness.BIG_ENDIAN.ordinal());
        assertThat(roundTrip(schemaOf(Painted.class), painted)).isEqualTo(painted);
        assertThat(roundTrip(named, painted)).isEqualTo(painted);
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
                .defaults(defaults -> defaults.set(SchemaKeys.ENDIANNESS, Endianness.LITTLE_ENDIAN))
                .field(Types.recordOf(Point.class), "point")
                .build();

        assertThat(written(out -> schema.write(out, List.of(new Point(1, 2))))).containsExactly(1, 0, 0, 0, 2, 0, 0, 0);
    }

    @Test
    void schemaDefaultsCanReplaceTheTypeOfAJavaType() {
        final Schema schema = varIntSchemaOf(Point.class);

        assertThat(written(out -> schema.write(out, List.of(new Point(1, 2)))))
                .as("int components as varints, registered as Integer")
                .containsExactly(1, 2);
    }

    @Test
    void replacedTypesApplyInsideNestedRecordsAndContainers() {
        final Schema schema = SchemaBuilder.create()
                .defaults(defaults -> defaults
                        .update(SchemaStandardKeys.RECORD_TYPES, types -> types.with(Integer.class, Types.VAR_INT))
                        .set(SchemaStandardKeys.LENGTH_TYPE, Types.INT)
                )
                .field(Types.recordOf(Leaderboard.class), "value")
                .build();
        final Leaderboard leaderboard = new Leaderboard(new Point(1, 2), List.of(300));

        assertThat(written(out -> schema.write(out, List.of(leaderboard))))
                .as("list length keeps the schema length type")
                .containsExactly(1, 2, 0, 0, 0, 1, 0xAC, 0x02);
        assertThat(roundTrip(schema, leaderboard)).isEqualTo(leaderboard);
    }

    @Test
    void recordTypesTreatPrimitivesAndWrappersAsTheSameType() {
        final Schema schema = SchemaBuilder.create()
                .defaults(defaults -> defaults.update(SchemaStandardKeys.RECORD_TYPES, types -> types.with(int.class, Types.VAR_INT)))
                .field(Types.recordOf(Point.class), "point")
                .build();

        assertThat(written(out -> schema.write(out, List.of(new Point(1, 2))))).containsExactly(1, 2);
    }

    @Test
    void recordTypesAddUpAcrossDefaultsCalls() {
        final Schema schema = SchemaBuilder.create()
                .defaults(defaults -> defaults.update(SchemaStandardKeys.RECORD_TYPES, types -> types.with(Integer.class, Types.VAR_INT)))
                .defaults(defaults -> defaults.update(SchemaStandardKeys.RECORD_TYPES, types -> types.with(Long.class, Types.VAR_LONG)))
                .field(Types.recordOf(Point.class), "point")
                .build();

        assertThat(written(out -> schema.write(out, List.of(new Point(1, 2))))).as("the Long type kept the Integer one").containsExactly(1, 2);
    }

    @Test
    void laterRecordTypeForAWrapperReplacesThePrimitiveOne() {
        final RecordTypes types = RecordTypes.empty().with(int.class, Types.VAR_INT).with(Integer.class, Types.INT);

        assertThat(types.types()).hasSize(1);
        assertThat(types.get(int.class)).containsSame(Types.INT);
    }

    @Test
    void recordTypesForAPrimitiveAndItsWrapperInOneMapAreRejected() {
        assertThatThrownBy(() -> new RecordTypes(Map.of(int.class, Types.VAR_INT, Integer.class, Types.INT)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Integer");
    }

    @Test
    void wireTypeAnnotationsWinOverSchemaDefaults() {
        final Schema schema = varIntSchemaOf(Mixed.class);

        assertThat(written(out -> schema.write(out, List.of(new Mixed(1, 2))))).containsExactly(1, 0, 0, 0, 2);
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
    void replacedTypesSurviveFuzzing() {
        final Schema schema = varIntSchemaOf(Leaderboard.class);

        final FuzzResult roundTrip = Fuzz.roundTrip(schema, 2_000, 42);
        final FuzzResult mutated = Fuzz.mutate(schema, 5_000, 42);

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
