// This file is part of BitFuzz.
// SPDX-FileCopyrightText: 2026 RedVortexDev
// SPDX-License-Identifier: Apache-2.0

package red.vortx.bitfuzz.schema.core;

import org.junit.jupiter.api.Test;
import red.vortx.bitfuzz.schema.api.Endianness;
import red.vortx.bitfuzz.schema.api.SchemaDefaults;
import red.vortx.bitfuzz.schema.api.key.SchemaKey;
import red.vortx.bitfuzz.schema.api.key.SchemaKeys;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SchemaDefaultsImplTest {

    private static final SchemaKey<String> NAME = new SchemaKey<>("name", "fallback");

    @Test
    void unsetKeysUseTheirFallback() {
        assertThat(new SchemaDefaultsImpl(Map.of()).get(NAME)).isEqualTo("fallback");
    }

    @Test
    void toMutableCopiesValues() {
        final SchemaDefaults defaults = new SchemaDefaultsImpl(Map.of(NAME, "set"));

        assertThat(defaults.toMutable().get(NAME)).isEqualTo("set");
    }

    @Test
    void mutatingCopyDoesNotChangeOriginal() {
        final SchemaDefaults defaults = new SchemaDefaultsImpl(Map.of(NAME, "set"));

        defaults.toMutable().set(NAME, "changed").set(SchemaKeys.ENDIANNESS, Endianness.LITTLE_ENDIAN);

        assertThat(defaults.get(NAME)).isEqualTo("set");
        assertThat(defaults.get(SchemaKeys.ENDIANNESS)).isEqualTo(Endianness.BIG_ENDIAN);
    }

    @Test
    void updateStartsFromTheFallback() {
        final SchemaDefaults.Mutable mutable = SchemaDefaultsImpl.empty().update(NAME, name -> name + " updated");

        assertThat(mutable.update(NAME, name -> name + " twice").get(NAME)).isEqualTo("fallback updated twice");
    }

    @Test
    void buildSnapshotsCurrentValues() {
        final SchemaDefaults.Mutable mutable = SchemaDefaultsImpl.empty().set(NAME, "first");
        final SchemaDefaults built = mutable.build();

        mutable.set(NAME, "second");

        assertThat(built.get(NAME)).isEqualTo("first");
        assertThat(mutable.get(NAME)).isEqualTo("second");
    }

    @Test
    void endiannessOpposite() {
        assertThat(Endianness.BIG_ENDIAN.opposite()).isEqualTo(Endianness.LITTLE_ENDIAN);
        assertThat(Endianness.LITTLE_ENDIAN.opposite()).isEqualTo(Endianness.BIG_ENDIAN);
        assertThat(Endianness.BIG_ENDIAN.isBigEndian()).isTrue();
        assertThat(Endianness.BIG_ENDIAN.isLittleEndian()).isFalse();
        assertThat(Endianness.LITTLE_ENDIAN.isLittleEndian()).isTrue();
        assertThat(Endianness.LITTLE_ENDIAN.isBigEndian()).isFalse();
    }

}
