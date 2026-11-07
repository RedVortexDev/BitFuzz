// This file is part of BitFuzz.
// SPDX-FileCopyrightText: 2026 RedVortexDev
// SPDX-License-Identifier: Apache-2.0

package red.vortx.bitfuzz.schema.std;

import org.junit.jupiter.api.Test;
import red.vortx.bitfuzz.schema.std.type.Types;

import static org.assertj.core.api.Assertions.assertThat;
import static red.vortx.bitfuzz.schema.std.TestSchemas.*;

class BoolTypeTest {

    @Test
    void encodesAsOneByte() {
        assertThat(encode(Types.BOOL, true)).containsExactly(bytes(1));
        assertThat(encode(Types.BOOL, false)).containsExactly(bytes(0));
    }

    @Test
    void roundTripAndSize() {
        assertRoundTrip(Types.BOOL, true);
        assertRoundTrip(Types.BOOL, false);
        assertSizeOfMatches(Types.BOOL, true);
    }

    @Test
    void decodesOnlyZeroAndOne() {
        assertThat(decode(Types.BOOL, bytes(0))).isEqualTo(false);
        assertThat(decode(Types.BOOL, bytes(1))).isEqualTo(true);
    }

    @Test
    void rejectsOtherBytes() {
        assertRejects(Types.BOOL, 2);
        assertRejects(Types.BOOL, 0xFF);
    }

    @Test
    void rejectsTruncated() {
        assertRejects(Types.BOOL);
    }

}
