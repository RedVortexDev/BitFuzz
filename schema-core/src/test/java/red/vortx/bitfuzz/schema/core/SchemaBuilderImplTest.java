// This file is part of BitFuzz.
// SPDX-FileCopyrightText: 2026 RedVortexDev
// SPDX-License-Identifier: Apache-2.0

package red.vortx.bitfuzz.schema.core;

import org.junit.jupiter.api.Test;
import red.vortx.bitfuzz.schema.api.Endianness;
import red.vortx.bitfuzz.schema.api.Schema;
import red.vortx.bitfuzz.schema.api.SchemaBuilder;
import red.vortx.bitfuzz.schema.api.SchemaDefaults;
import red.vortx.bitfuzz.schema.api.field.Field;
import red.vortx.bitfuzz.schema.api.field.type.EndiannessTypeSpec;
import red.vortx.bitfuzz.schema.api.field.type.Primitives;
import red.vortx.bitfuzz.schema.api.field.type.TypeSpec;
import red.vortx.bitfuzz.schema.api.key.SchemaKey;
import red.vortx.bitfuzz.schema.api.key.SchemaKeys;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class SchemaBuilderImplTest {

    private static final TypeSpec<Integer> INT = new EndiannessTypeSpec<>(Primitives.INT, Primitives.INT_LE);
    private static final SchemaKey<TypeSpec<Integer>> LENGTH = new SchemaKey<>("length", Primitives.INT);

    private static SchemaDefaults defaultsOf(final Schema schema) {
        return ((SchemaImpl) schema).defaults();
    }

    @Test
    void defaultsAreBigEndian() {
        final Schema schema = new SchemaBuilderImpl().field(INT, "a").build();

        assertThat(defaultsOf(schema).get(SchemaKeys.ENDIANNESS)).isEqualTo(Endianness.BIG_ENDIAN);
        assertThat(schema.fields().getFirst().type()).isSameAs(Primitives.INT);
    }

    @Test
    void endiannessSpecFollowsDefaults() {
        final Schema schema = new SchemaBuilderImpl()
                .defaults(defaults -> defaults.set(SchemaKeys.ENDIANNESS, Endianness.LITTLE_ENDIAN))
                .field(INT, "a")
                .build();

        assertThat(schema.fields().getFirst().type()).isSameAs(Primitives.INT_LE);
    }

    @Test
    void defaultsApplyRegardlessOfCallOrder() {
        final Schema schema = new SchemaBuilderImpl()
                .field(INT, "a")
                .defaults(defaults -> defaults.set(SchemaKeys.ENDIANNESS, Endianness.LITTLE_ENDIAN))
                .build();

        assertThat(schema.fields().getFirst().type()).isSameAs(Primitives.INT_LE);
    }

    @Test
    void laterDefaultsCallsOverrideEarlierOnes() {
        final Schema schema = new SchemaBuilderImpl()
                .defaults(defaults -> defaults.set(SchemaKeys.ENDIANNESS, Endianness.LITTLE_ENDIAN))
                .defaults(defaults -> defaults.set(SchemaKeys.ENDIANNESS, Endianness.BIG_ENDIAN))
                .field(INT, "a")
                .build();

        assertThat(schema.fields().getFirst().type()).isSameAs(Primitives.INT);
    }

    @Test
    void pinnedTypesIgnoreDefaults() {
        final Schema schema = new SchemaBuilderImpl()
                .defaults(defaults -> defaults.set(SchemaKeys.ENDIANNESS, Endianness.LITTLE_ENDIAN))
                .field(Primitives.INT, "pinned")
                .build();

        assertThat(schema.fields().getFirst().type()).isSameAs(Primitives.INT);
    }

    @Test
    void fieldsKeepNamesAndOrder() {
        final Schema schema = new SchemaBuilderImpl()
                .field(Primitives.BYTE, "first")
                .field(Primitives.LONG, "second")
                .field(Primitives.BYTE, "third")
                .build();

        assertThat(schema.fields()).extracting(Field::name).containsExactly("first", "second", "third");
    }

    @Test
    void duplicateFieldNamesAreAllowed() {
        final Schema schema = new SchemaBuilderImpl()
                .field(Primitives.BYTE, "same")
                .field(Primitives.BYTE, "same")
                .build();

        assertThat(schema.fields()).hasSize(2);
    }

    @Test
    void specsAreResolvedOnceAtBuild() {
        final AtomicInteger resolutions = new AtomicInteger();
        final TypeSpec<Integer> counting = defaults -> {
            resolutions.incrementAndGet();
            return Primitives.INT;
        };

        final SchemaBuilder builder = new SchemaBuilderImpl().field(counting, "a");
        assertThat(resolutions).hasValue(0);

        final Schema schema = builder.build();
        assertThat(resolutions).hasValue(1);

        schema.fields();
        assertThat(resolutions).hasValue(1);
    }

    @Test
    void builtSchemaIsUnaffectedByLaterBuilderChanges() {
        final SchemaBuilder builder = new SchemaBuilderImpl().field(INT, "a");
        final Schema first = builder.build();

        builder.defaults(defaults -> defaults.set(SchemaKeys.ENDIANNESS, Endianness.LITTLE_ENDIAN)).field(INT, "b");
        final Schema second = builder.build();

        assertThat(first.fields()).hasSize(1);
        assertThat(first.fields().getFirst().type()).isSameAs(Primitives.INT);
        assertThat(defaultsOf(first).get(SchemaKeys.ENDIANNESS)).isEqualTo(Endianness.BIG_ENDIAN);

        assertThat(second.fields()).hasSize(2);
        assertThat(second.fields()).extracting(Field::type).containsOnly(Primitives.INT_LE);
    }

    @Test
    void keysAreVisibleToSpecs() {
        final TypeSpec<Integer> lengthOfSchema = defaults -> defaults.get(LENGTH).resolve(defaults);

        final Schema schema = new SchemaBuilderImpl()
                .defaults(defaults -> defaults.set(LENGTH, INT))
                .defaults(defaults -> defaults.set(SchemaKeys.ENDIANNESS, Endianness.LITTLE_ENDIAN))
                .field(lengthOfSchema, "length")
                .build();

        assertThat(schema.fields().getFirst().type()).as("resolved against the final endianness").isSameAs(Primitives.INT_LE);
    }

    @Test
    void unsetKeysUseTheirFallbackInSpecs() {
        final TypeSpec<Integer> lengthOfSchema = defaults -> defaults.get(LENGTH).resolve(defaults);

        final Schema schema = new SchemaBuilderImpl().field(lengthOfSchema, "length").build();

        assertThat(schema.fields().getFirst().type()).isSameAs(Primitives.INT);
    }

}
