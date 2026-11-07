// This file is part of BitFuzz.
// SPDX-FileCopyrightText: 2026 RedVortexDev
// SPDX-License-Identifier: Apache-2.0

package red.vortx.bitfuzz.schema.std;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import red.vortx.bitfuzz.schema.api.Endianness;
import red.vortx.bitfuzz.schema.api.Schema;
import red.vortx.bitfuzz.schema.api.field.type.Primitives;
import red.vortx.bitfuzz.schema.api.field.type.Type;
import red.vortx.bitfuzz.schema.api.field.type.TypeSpec;
import red.vortx.bitfuzz.schema.api.key.SchemaKeys;
import red.vortx.bitfuzz.schema.core.SchemaBuilderImpl;
import red.vortx.bitfuzz.schema.std.type.Types;

import static org.assertj.core.api.Assertions.assertThat;

class TypesTest {

    private static Type<?> resolve(final TypeSpec<?> spec, final Endianness endianness) {
        final Schema schema = new SchemaBuilderImpl()
                .defaults(defaults -> defaults.set(SchemaKeys.ENDIANNESS, endianness))
                .field(spec, "x")
                .build();
        return schema.fields().getFirst().type();
    }

    @ParameterizedTest
    @EnumSource(Endianness.class)
    void specsFollowSchemaEndianness(final Endianness endianness) {
        final boolean big = endianness.isBigEndian();

        assertThat(resolve(Types.SHORT, endianness)).isSameAs(big ? Types.SHORT_BE : Types.SHORT_LE);
        assertThat(resolve(Types.INT, endianness)).isSameAs(big ? Types.INT_BE : Types.INT_LE);
        assertThat(resolve(Types.LONG, endianness)).isSameAs(big ? Types.LONG_BE : Types.LONG_LE);
        assertThat(resolve(Types.FLOAT, endianness)).isSameAs(big ? Types.FLOAT_BE : Types.FLOAT_LE);
        assertThat(resolve(Types.DOUBLE, endianness)).isSameAs(big ? Types.DOUBLE_BE : Types.DOUBLE_LE);
    }

    @ParameterizedTest
    @EnumSource(Endianness.class)
    void pinnedTypesIgnoreSchemaEndianness(final Endianness endianness) {
        assertThat(resolve(Types.BYTE, endianness)).isSameAs(Primitives.BYTE);
        assertThat(resolve(Types.INT_BE, endianness)).isSameAs(Primitives.INT);
        assertThat(resolve(Types.INT_LE, endianness)).isSameAs(Primitives.INT_LE);
    }

    @ParameterizedTest
    @EnumSource(Endianness.class)
    void aliasesMatchPrimitives(final Endianness ignored) {
        assertThat(Types.SHORT_BE).isSameAs(Primitives.SHORT);
        assertThat(Types.SHORT_LE).isSameAs(Primitives.SHORT_LE);
        assertThat(Types.INT_BE).isSameAs(Primitives.INT);
        assertThat(Types.INT_LE).isSameAs(Primitives.INT_LE);
        assertThat(Types.LONG_BE).isSameAs(Primitives.LONG);
        assertThat(Types.LONG_LE).isSameAs(Primitives.LONG_LE);
        assertThat(Types.FLOAT_BE).isSameAs(Primitives.FLOAT);
        assertThat(Types.FLOAT_LE).isSameAs(Primitives.FLOAT_LE);
        assertThat(Types.DOUBLE_BE).isSameAs(Primitives.DOUBLE);
        assertThat(Types.DOUBLE_LE).isSameAs(Primitives.DOUBLE_LE);
    }

}
