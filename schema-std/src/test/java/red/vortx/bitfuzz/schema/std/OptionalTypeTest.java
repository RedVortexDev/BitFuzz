// This file is part of BitFuzz.
// SPDX-FileCopyrightText: 2026 RedVortexDev
// SPDX-License-Identifier: Apache-2.0

package red.vortx.bitfuzz.schema.std;

import org.junit.jupiter.api.Test;
import red.vortx.bitfuzz.schema.api.Endianness;
import red.vortx.bitfuzz.schema.api.field.type.TypeSpec;
import red.vortx.bitfuzz.schema.std.type.Types;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static red.vortx.bitfuzz.schema.std.TestSchemas.*;

class OptionalTypeTest {

    private static final TypeSpec<Optional<Integer>> OPTIONAL_INT = Types.optionalOf(Types.INT);

    @Test
    void emptyIsSingleZeroByte() {
        assertThat(encode(OPTIONAL_INT, Optional.empty())).containsExactly(bytes(0));
        assertThat(decode(OPTIONAL_INT, bytes(0))).isEqualTo(Optional.empty());
    }

    @Test
    void presentIsOneThenValue() {
        assertThat(encode(OPTIONAL_INT, Optional.of(5))).containsExactly(bytes(1, 0, 0, 0, 5));
        assertThat(decode(OPTIONAL_INT, bytes(1, 0, 0, 0, 5))).isEqualTo(Optional.of(5));
    }

    @Test
    void valueFollowsSchemaEndianness() {
        assertThat(encode(OPTIONAL_INT, Endianness.LITTLE_ENDIAN, Optional.of(5))).containsExactly(bytes(1, 5, 0, 0, 0));
    }

    @Test
    void roundTripAndSize() {
        assertRoundTrip(OPTIONAL_INT, Optional.empty());
        assertRoundTrip(OPTIONAL_INT, Optional.of(-1));
        assertRoundTrip(Types.optionalOf(Types.STRING), Optional.of("hi"));
        assertSizeOfMatches(OPTIONAL_INT, Optional.empty());
        assertSizeOfMatches(OPTIONAL_INT, Optional.of(1));
    }

    @Test
    void nestedOptionals() {
        final TypeSpec<Optional<Optional<Byte>>> nested = Types.optionalOf(Types.optionalOf(Types.BYTE));

        assertThat(encode(nested, Optional.of(Optional.empty()))).containsExactly(bytes(1, 0));
        assertRoundTrip(nested, Optional.empty());
        assertRoundTrip(nested, Optional.of(Optional.empty()));
        assertRoundTrip(nested, Optional.of(Optional.of((byte) 7)));
    }

    @Test
    void rejectsInvalidPresenceByte() {
        assertRejects(OPTIONAL_INT, 2);
        assertRejects(OPTIONAL_INT, 0xFF, 0, 0, 0, 5);
    }

    @Test
    void rejectsMissingValue() {
        assertRejects(OPTIONAL_INT, 1, 0, 0);
        assertRejects(OPTIONAL_INT);
    }

}
