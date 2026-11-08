// This file is part of BitFuzz.
// SPDX-FileCopyrightText: 2026 RedVortexDev
// SPDX-License-Identifier: Apache-2.0

package red.vortx.bitfuzz.schema.std;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import red.vortx.bitfuzz.fuzz.api.FuzzResult;
import red.vortx.bitfuzz.fuzz.core.Fuzz;
import red.vortx.bitfuzz.schema.api.Endianness;
import red.vortx.bitfuzz.schema.api.Schema;
import red.vortx.bitfuzz.schema.api.SchemaBuilder;
import red.vortx.bitfuzz.schema.api.SchemaException;
import red.vortx.bitfuzz.schema.api.field.type.Type;
import red.vortx.bitfuzz.schema.std.type.StandardType;
import red.vortx.bitfuzz.schema.std.type.Types;
import red.vortx.bitfuzz.schema.std.type.record.RecordMapper;
import red.vortx.bitfuzz.schema.std.type.record.VarInt;
import red.vortx.bitfuzz.schema.std.type.record.VarLong;

import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/// Fuzzes realistic schemas built from every standard type.
///
/// - Round trip: random valid values must decode to themselves and consume every byte.
/// - Mutation: damaged encodings must decode or be rejected with [SchemaException], never crash.
///
/// Set `-Dbitfuzz.seed=<n>` to try other seeds locally; the default keeps runs reproducible.
@Timeout(120)
class StandardTypesFuzzTest {

    private static final long SEED = Long.getLong("bitfuzz.seed", 42L);

    static Stream<Schema> schemas() {
        return Stream.of(
                SchemaBuilder.create()
                        .field(Types.VAR_INT, "id")
                        .field(Types.STRING, "name")
                        .field(Types.listOf(Types.optionalOf(Types.UUID)), "friends")
                        .field(Types.mapOf(Types.STRING, Types.LONG), "stats")
                        .field(Types.BOOL, "online")
                        .build(),
                SchemaBuilder.create()
                        .field(Types.listOf(Types.mapOf(Types.VAR_LONG, Types.listOf(Types.STRING))), "nested")
                        .build(),
                SchemaBuilder.create()
                        .field(Types.optionalOf(Types.listOf(Types.optionalOf(Types.stringOf(Types.VAR_INT)))), "deep")
                        .field(Types.mapOf(Types.BOOL, Types.DOUBLE), "flags")
                        .build(),
                SchemaBuilder.create()
                        .field(Types.SHORT, "short")
                        .field(Types.INT_LE, "int")
                        .field(Types.LONG, "long")
                        .field(Types.FLOAT, "float")
                        .field(Types.DOUBLE_LE, "double")
                        .field(Types.VAR_INT, "varint")
                        .field(Types.VAR_LONG, "varlong")
                        .field(Types.BYTE, "byte")
                        .build(),
                SchemaBuilder.create()
                        .field(Types.enumOrdinalOf(Endianness.class), "ordinal")
                        .field(Types.listOf(Types.enumNameOf(Endianness.class)), "names")
                        .field(Types.fixedString(4), "fixed")
                        .build(),
                SchemaBuilder.create()
                        .field(Types.recordOf(Everything.class), "everything")
                        .build()
        );
    }

    @Test
    void schemasCoverEveryStandardType() {
        final Set<Class<?>> used = new HashSet<>();
        schemas().forEach(schema -> schema.fields().forEach(field -> collectTypes(field.type(), used)));

        assertThat(used)
                .as("every standard type appears in a fuzzed schema")
                .containsAll(findLeaves(StandardType.class));
    }

    @ParameterizedTest
    @MethodSource("schemas")
    void roundTrip(final Schema schema) {
        final FuzzResult result = Fuzz.roundTrip(schema, 10_000, SEED);

        assertThat(result.isClean()).as(result.report()).isTrue();
    }

    @ParameterizedTest
    @MethodSource("schemas")
    void survivesMutation(final Schema schema) {
        final FuzzResult result = Fuzz.mutate(schema, 25_000, SEED);

        assertThat(result.isClean()).as(result.report()).isTrue();
    }

    private static void collectTypes(final Object node, final Set<Class<?>> used) {
        switch (node) {
            case final Type<?> type when type instanceof Record -> {
                used.add(type.getClass());
                collectComponents((Record) type, used);
            }
            case final Type<?> type -> used.add(type.getClass());
            case final Record record -> collectComponents(record, used);
            case final Collection<?> collection -> collection.forEach(element -> collectTypes(element, used));
            default -> {
            }
        }
    }

    private static void collectComponents(final Record record, final Set<Class<?>> used) {
        for (final RecordComponent component : record.getClass().getRecordComponents()) {
            try {
                final Method accessor = component.getAccessor();
                accessor.setAccessible(true);
                collectTypes(accessor.invoke(record), used);
            } catch (final ReflectiveOperationException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    private static Set<Class<?>> findLeaves(final Class<?> sealedClass) {
        final Set<Class<?>> leaves = new HashSet<>();

        for (final Class<?> permitted : sealedClass.getPermittedSubclasses()) {
            if (permitted.isSealed()) {
                leaves.addAll(findLeaves(permitted));
            } else {
                leaves.add(permitted);
            }
        }

        return leaves;
    }

    /// One component of every Java type records map on their own. Add one when [RecordMapper] learns a new type.
    record Everything(
            byte b, short s, int i, long l, float f, double d, boolean bool,
            String string, UUID uuid, Endianness order,
            @VarInt int varInt, @VarLong long varLong,
            Inner inner, List<String> list, Optional<UUID> optional, Map<String, Integer> map
    ) {

    }

    record Inner(int value) {

    }

}
