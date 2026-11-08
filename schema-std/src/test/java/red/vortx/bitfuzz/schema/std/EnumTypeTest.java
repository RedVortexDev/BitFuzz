// This file is part of BitFuzz.
// SPDX-FileCopyrightText: 2026 RedVortexDev
// SPDX-License-Identifier: Apache-2.0

package red.vortx.bitfuzz.schema.std;

import org.junit.jupiter.api.Test;
import red.vortx.bitfuzz.schema.api.Schema;
import red.vortx.bitfuzz.schema.api.SchemaBuilder;
import red.vortx.bitfuzz.schema.api.field.type.TypeSpec;
import red.vortx.bitfuzz.schema.std.key.SchemaStandardKeys;
import red.vortx.bitfuzz.schema.std.type.Types;
import red.vortx.bitfuzz.schema.std.type.enumeration.EnumEncoding;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static red.vortx.bitfuzz.schema.std.TestBuffers.written;
import static red.vortx.bitfuzz.schema.std.TestSchemas.*;

class EnumTypeTest {

    private static final TypeSpec<Color> ORDINAL = Types.enumOrdinalOf(Color.class);
    private static final TypeSpec<Color> NAME = Types.enumNameOf(Color.class);

    @Test
    void ordinalIsVarIntByDefault() {
        assertThat(encode(ORDINAL, Color.GREEN)).containsExactly(bytes(1));
        assertThat(decode(ORDINAL, bytes(2))).isEqualTo(Color.BLUE);
    }

    @Test
    void ordinalUsesGivenTagType() {
        assertThat(encode(Types.enumOrdinalOf(Color.class, Types.INT), Color.GREEN)).containsExactly(bytes(0, 0, 0, 1));
    }

    @Test
    void nameIsLengthPrefixedString() {
        assertThat(encode(NAME, Color.RED)).containsExactly(bytes(3, 'R', 'E', 'D'));
        assertThat(decode(NAME, bytes(4, 'B', 'L', 'U', 'E'))).isEqualTo(Color.BLUE);
    }

    @Test
    void enumOfFollowsSchemaEncoding() {
        final Schema schema = SchemaBuilder.create()
                .defaults(defaults -> defaults.set(SchemaStandardKeys.ENUM_ENCODING, EnumEncoding.NAME))
                .field(Types.enumOf(Color.class), "color")
                .build();

        assertThat(written(out -> schema.write(out, List.of(Color.RED)))).containsExactly(3, 'R', 'E', 'D');
        assertThat(encode(Types.enumOf(Color.class), Color.BLUE)).as("ordinal by default").containsExactly(bytes(2));
    }

    @Test
    void constantsWithBodiesUseTheirEnumClass() {
        assertRoundTrip(ORDINAL, Color.SPECIAL);
        assertRoundTrip(NAME, Color.SPECIAL);
    }

    @Test
    void roundTripAndSize() {
        for (final Color color : Color.values()) {
            assertRoundTrip(ORDINAL, color);
            assertRoundTrip(NAME, color);
            assertSizeOfMatches(ORDINAL, color);
            assertSizeOfMatches(NAME, color);
        }
    }

    @Test
    void rejectsUnknownOrdinal() {
        assertRejects(ORDINAL, 4);
        assertRejects(Types.enumOrdinalOf(Color.class, Types.INT), 0xFF, 0xFF, 0xFF, 0xFF);
        assertRejects(ORDINAL);
    }

    @Test
    void rejectsUnknownName() {
        assertRejects(NAME, 3, 'r', 'e', 'd');
        assertRejects(NAME, 0);
        assertRejects(NAME, 3, 'R', 'E');
    }

    private enum Color {
        RED,
        GREEN,
        BLUE,
        SPECIAL {
            @Override
            public String toString() {
                return "special";
            }
        }
    }

}
