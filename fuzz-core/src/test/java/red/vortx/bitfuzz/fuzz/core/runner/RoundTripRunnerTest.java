// This file is part of BitFuzz.
// SPDX-FileCopyrightText: 2026 RedVortexDev
// SPDX-License-Identifier: Apache-2.0

package red.vortx.bitfuzz.fuzz.core.runner;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import red.vortx.bitfuzz.fuzz.api.FuzzFailure;
import red.vortx.bitfuzz.fuzz.api.FuzzResult;
import red.vortx.bitfuzz.fuzz.core.generator.Generators;
import red.vortx.bitfuzz.schema.api.Schema;
import red.vortx.bitfuzz.schema.api.SchemaBuilder;
import red.vortx.bitfuzz.schema.std.type.Types;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(30)
class RoundTripRunnerTest {

    private static FuzzResult run(final Schema schema, final int iterations, final long seed) {
        return new RoundTripRunner(schema, Generators.standard()).run(iterations, seed);
    }

    @Test
    void standardTypesAreClean() {
        final Schema schema = SchemaBuilder.create()
                .field(Types.VAR_INT, "id")
                .field(Types.STRING, "name")
                .field(Types.map(Types.STRING, Types.list(Types.optional(Types.DOUBLE))), "data")
                .build();

        final FuzzResult result = run(schema, 5_000, 42);

        assertThat(result.isClean()).as(result.report()).isTrue();
        assertThat(result.iterations()).isEqualTo(5_000);
    }

    @Test
    void catchesValuesThatChange() {
        final Schema schema = SchemaBuilder.create().field(BuggyTypes.LOSSY_INT, "lossy").build();

        final FuzzResult result = run(schema, 1_000, 42);

        assertThat(result.isClean()).isFalse();
        assertThat(result.failures().getFirst().failure())
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("decoded");
    }

    @Test
    void catchesLeftoverBytes() {
        final Schema schema = SchemaBuilder.create().field(BuggyTypes.PADDED_BOOL, "padded").build();

        final FuzzResult result = run(schema, 100, 42);

        assertThat(result.isClean()).isFalse();
        assertThat(result.failures().getFirst().failure()).hasMessageContaining("left unread");
    }

    @Test
    void stopsAfterMaxFailures() {
        final Schema schema = SchemaBuilder.create().field(BuggyTypes.PADDED_BOOL, "padded").build();

        final FuzzResult result = run(schema, 10_000, 42);

        assertThat(result.failures()).hasSize(RoundTripRunner.DEFAULT_MAX_FAILURES);
        assertThat(result.iterations()).isEqualTo(RoundTripRunner.DEFAULT_MAX_FAILURES);
    }

    @Test
    void sameSeedFindsSameFailures() {
        final Schema schema = SchemaBuilder.create().field(BuggyTypes.LOSSY_INT, "lossy").build();

        final List<Long> first = run(schema, 1_000, 7).failures().stream().map(FuzzFailure::seed).toList();
        final List<Long> second = run(schema, 1_000, 7).failures().stream().map(FuzzFailure::seed).toList();

        assertThat(first).isNotEmpty().isEqualTo(second);
    }

}
