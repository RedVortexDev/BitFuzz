// This file is part of BitFuzz.
// SPDX-FileCopyrightText: 2026 RedVortexDev
// SPDX-License-Identifier: Apache-2.0

package red.vortx.bitfuzz.schema.core;

import org.junit.jupiter.api.Test;
import red.vortx.bitfuzz.schema.api.Endianness;
import red.vortx.bitfuzz.schema.api.key.SchemaKey;
import red.vortx.bitfuzz.schema.api.key.SchemaKeys;

import static org.assertj.core.api.Assertions.assertThat;

class SchemaDefaultsInitialTest {

    private static final SchemaKey<String> NAME = new SchemaKey<>("name", "fallback");

    @Test
    void initialHasEveryKeyAtItsFallback() {
        assertThat(SchemaDefaultsImpl.empty().get(SchemaKeys.ENDIANNESS)).isEqualTo(Endianness.BIG_ENDIAN);
        assertThat(SchemaDefaultsImpl.empty().get(NAME)).isEqualTo("fallback");
        assertThat(SchemaDefaultsImpl.empty().build().get(NAME)).isEqualTo("fallback");
    }

    @Test
    void initialReturnsIndependentCopies() {
        SchemaDefaultsImpl.empty().set(SchemaKeys.ENDIANNESS, Endianness.LITTLE_ENDIAN);

        assertThat(SchemaDefaultsImpl.empty().get(SchemaKeys.ENDIANNESS)).isEqualTo(Endianness.BIG_ENDIAN);
    }

}
