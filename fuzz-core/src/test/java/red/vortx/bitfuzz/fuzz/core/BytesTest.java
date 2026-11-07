// This file is part of BitFuzz.
// SPDX-FileCopyrightText: 2026 RedVortexDev
// SPDX-License-Identifier: Apache-2.0

package red.vortx.bitfuzz.fuzz.core;

import org.junit.jupiter.api.Test;
import red.vortx.bitfuzz.schema.api.Schema;
import red.vortx.bitfuzz.schema.api.SchemaBuilder;
import red.vortx.bitfuzz.schema.std.type.Types;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BytesTest {

    private static final Schema SCHEMA = SchemaBuilder.create()
            .field(Types.VAR_INT, "id")
            .field(Types.STRING, "name")
            .build();

    @Test
    void encodeIsExactlySized() {
        assertThat(Bytes.encode(SCHEMA, List.of(300, "hi"))).hasSize(2 + 1 + 2);
    }

    @Test
    void roundTrip() {
        final List<Object> values = List.of(-1, "日本語");

        assertThat(Bytes.decode(SCHEMA, Bytes.encode(SCHEMA, values))).isEqualTo(values);
    }

    @Test
    void hex() {
        assertThat(Bytes.hex(new byte[]{0x7F, (byte) 0xFF, 0x00})).isEqualTo("7f ff 00");
        assertThat(Bytes.hex(new byte[0])).isEmpty();
    }

}
