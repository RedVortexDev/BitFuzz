// This file is part of BitFuzz.
// SPDX-FileCopyrightText: 2026 RedVortexDev
// SPDX-License-Identifier: Apache-2.0

package red.vortx.bitfuzz.schema.std;

import org.junit.jupiter.api.Test;
import red.vortx.bitfuzz.schema.std.type.ListType;
import red.vortx.bitfuzz.schema.std.type.MapType;
import red.vortx.bitfuzz.schema.std.type.OptionalType;
import red.vortx.bitfuzz.schema.std.type.StringType;
import red.vortx.bitfuzz.schema.std.type.Types;

import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class NullSafetyTest {

    @Test
    void typeRecordsRejectNulls() {
        assertThatNullPointerException()
                .isThrownBy(() -> new ListType<>(null, Types.INT_BE))
                .withMessage("elementType");
        assertThatNullPointerException().isThrownBy(() -> new ListType<>(Types.INT_BE, null)).withMessage("lengthType");
        assertThatNullPointerException()
                .isThrownBy(() -> new MapType<>(null, Types.INT_BE, Types.INT_BE))
                .withMessage("keyType");
        assertThatNullPointerException().isThrownBy(() -> new OptionalType<>(null)).withMessage("valueType");
        assertThatNullPointerException().isThrownBy(() -> new StringType(null)).withMessage("lengthType");
    }

    @Test
    void factoriesRejectNullsImmediately() {
        assertThatNullPointerException().isThrownBy(() -> Types.listOf(null)).withMessage("elementType");
        assertThatNullPointerException().isThrownBy(() -> Types.listOf(Types.INT, null)).withMessage("lengthType");
        assertThatNullPointerException().isThrownBy(() -> Types.mapOf(Types.INT, null)).withMessage("valueType");
        assertThatNullPointerException().isThrownBy(() -> Types.optionalOf(null)).withMessage("valueType");
        assertThatNullPointerException().isThrownBy(() -> Types.stringOf(null)).withMessage("lengthType");
    }

}
