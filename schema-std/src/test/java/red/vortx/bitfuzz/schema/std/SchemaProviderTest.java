// This file is part of BitFuzz.
// SPDX-FileCopyrightText: 2026 RedVortexDev
// SPDX-License-Identifier: Apache-2.0

package red.vortx.bitfuzz.schema.std;

import org.junit.jupiter.api.Test;
import red.vortx.bitfuzz.schema.api.Schema;
import red.vortx.bitfuzz.schema.api.SchemaBuilder;
import red.vortx.bitfuzz.schema.std.type.Types;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static red.vortx.bitfuzz.schema.std.TestBuffers.reader;
import static red.vortx.bitfuzz.schema.std.TestBuffers.written;

class SchemaProviderTest {

    @Test
    void schemaBuiltThroughApiRoundTrips() {
        final Schema schema = SchemaBuilder.create()
                .field(Types.INT, "id")
                .field(Types.listOf(Types.SHORT), "values")
                .build();
        final List<Object> values = List.of(7, List.of((short) 1, (short) 2));

        final byte[] bytes = written(out -> schema.write(out, values));

        assertThat(schema.read(reader(bytes))).containsExactlyElementsOf(values);
    }

}
