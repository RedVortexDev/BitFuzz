// This file is part of BitFuzz.
// SPDX-FileCopyrightText: 2026 RedVortexDev
// SPDX-License-Identifier: Apache-2.0

package red.vortx.bitfuzz.fuzz.core.generator;

import org.junit.jupiter.api.Test;
import red.vortx.bitfuzz.fuzz.core.Bytes;
import red.vortx.bitfuzz.fuzz.core.generator.impl.SchemaGenerator;
import red.vortx.bitfuzz.schema.api.Schema;
import red.vortx.bitfuzz.schema.api.SchemaBuilder;
import red.vortx.bitfuzz.schema.std.type.Types;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static red.vortx.bitfuzz.fuzz.core.generator.Samples.random;
import static red.vortx.bitfuzz.fuzz.core.generator.Samples.sample;

class SchemaGeneratorTest {

    private static final Schema PLAYER = SchemaBuilder.create()
            .field(Types.VAR_INT, "id")
            .field(Types.STRING, "name")
            .field(Types.list(Types.optional(Types.UUID)), "friends")
            .field(Types.map(Types.STRING, Types.LONG), "stats")
            .field(Types.BOOL, "online")
            .build();

    @Test
    void generatesOneValuePerFieldInOrder() {
        final List<Object> values = SchemaGenerator.of(PLAYER, Generators.standard()).generate(random());

        assertThat(values).hasSize(5);
        assertThat(values.get(0)).isInstanceOf(Integer.class);
        assertThat(values.get(1)).isInstanceOf(String.class);
        assertThat(values.get(2)).isInstanceOf(List.class);
        assertThat(values.get(3)).isInstanceOf(Map.class);
        assertThat(values.get(4)).isInstanceOf(Boolean.class);
    }

    @Test
    void generatedValuesAreWritableAndRoundTrip() {
        final SchemaGenerator generator = SchemaGenerator.of(PLAYER, Generators.standard());

        for (final List<Object> values : sample(generator, random(), 1_000)) {
            assertThat(Bytes.decode(PLAYER, Bytes.encode(PLAYER, values))).isEqualTo(values);
        }
    }

    @Test
    void nestedContentsHaveTheRightTypes() {
        final SchemaGenerator generator = SchemaGenerator.of(PLAYER, Generators.standard());

        for (final List<Object> values : sample(generator, random(), 200)) {
            assertThat((List<?>) values.get(2)).allSatisfy(friend -> {
                assertThat(friend).isInstanceOf(Optional.class);
                ((Optional<?>) friend).ifPresent(uuid -> assertThat(uuid).isInstanceOf(UUID.class));
            });
            assertThat((Map<?, ?>) values.get(3)).allSatisfy((key, value) -> {
                assertThat(key).isInstanceOf(String.class);
                assertThat(value).isInstanceOf(Long.class);
            });
        }
    }

    @Test
    void emptySchemaGeneratesEmptyList() {
        final Schema empty = SchemaBuilder.create().build();

        assertThat(SchemaGenerator.of(empty, Generators.standard()).generate(random())).isEmpty();
    }

}
