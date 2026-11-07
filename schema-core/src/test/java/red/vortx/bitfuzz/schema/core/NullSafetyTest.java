// This file is part of BitFuzz.
// SPDX-FileCopyrightText: 2026 RedVortexDev
// SPDX-License-Identifier: Apache-2.0

package red.vortx.bitfuzz.schema.core;

import org.junit.jupiter.api.Test;
import red.vortx.bitfuzz.schema.api.Endianness;
import red.vortx.bitfuzz.schema.api.Schema;
import red.vortx.bitfuzz.schema.api.SchemaDefaults;
import red.vortx.bitfuzz.schema.api.key.SchemaKey;
import red.vortx.bitfuzz.schema.api.key.SchemaKeys;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static red.vortx.bitfuzz.schema.core.TestBuffers.reader;
import static red.vortx.bitfuzz.schema.core.TestBuffers.written;

class NullSafetyTest {

    @Test
    void builderRejectsNullFieldParts() {
        assertThatNullPointerException().isThrownBy(() -> new SchemaBuilderImpl().field(null, "a")).withMessage("type");
        assertThatNullPointerException()
                .isThrownBy(() -> new SchemaBuilderImpl().field(Primitives.INT, null))
                .withMessage("name");
    }

    @Test
    void builderRejectsNullDefaultsConsumer() {
        assertThatNullPointerException()
                .isThrownBy(() -> new SchemaBuilderImpl().defaults(null))
                .withMessage("defaults");
    }

    @Test
    void defaultsRejectNullSettings() {
        assertThatNullPointerException()
                .isThrownBy(() -> new SchemaBuilderImpl().defaults(defaults -> defaults.set(null, Endianness.BIG_ENDIAN)))
                .withMessage("key");
        assertThatNullPointerException()
                .isThrownBy(() -> new SchemaBuilderImpl().defaults(defaults -> defaults.set(SchemaKeys.ENDIANNESS, null)))
                .withMessage("value");
        assertThatNullPointerException().isThrownBy(() -> SchemaDefaultsImpl.empty().get(null)).withMessage("key");
        assertThatNullPointerException()
                .isThrownBy(() -> SchemaDefaultsImpl.empty().update(SchemaKeys.ENDIANNESS, null))
                .withMessage("updater");
        assertThatNullPointerException().isThrownBy(() -> new SchemaKey<>(null, 1)).withMessage("name");
        assertThatNullPointerException().isThrownBy(() -> new SchemaKey<>("key", null)).withMessage("fallback");
    }

    @Test
    void schemaRejectsNullArguments() {
        final Schema schema = new SchemaBuilderImpl().field(Primitives.INT, "a").build();

        assertThatNullPointerException().isThrownBy(() -> schema.write(null, List.of(1))).withMessage("output");
        assertThatNullPointerException()
                .isThrownBy(() -> written(out -> schema.write(out, null)))
                .withMessage("values");
        assertThatNullPointerException().isThrownBy(() -> schema.read(null)).withMessage("input");
        assertThat(schema.read(reader(0, 0, 0, 1))).containsExactly(1);
    }

    @Test
    void toMutableOnMutableReturnsIndependentCopy() {
        final SchemaDefaults.Mutable original = SchemaDefaultsImpl.empty();
        final SchemaDefaults.Mutable copy = original.toMutable();

        copy.set(SchemaKeys.ENDIANNESS, Endianness.LITTLE_ENDIAN);

        assertThat(copy).isNotSameAs(original);
        assertThat(original.get(SchemaKeys.ENDIANNESS)).isEqualTo(Endianness.BIG_ENDIAN);
    }

}
