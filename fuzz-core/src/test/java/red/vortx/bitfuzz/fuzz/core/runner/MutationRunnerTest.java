package red.vortx.bitfuzz.fuzz.core.runner;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import red.vortx.bitfuzz.fuzz.api.FuzzFailure;
import red.vortx.bitfuzz.fuzz.api.FuzzResult;
import red.vortx.bitfuzz.fuzz.core.Bytes;
import red.vortx.bitfuzz.fuzz.core.generator.Generators;
import red.vortx.bitfuzz.fuzz.core.mutator.Mutators;
import red.vortx.bitfuzz.fuzz.core.shrink.ByteShrinker;
import red.vortx.bitfuzz.schema.api.Schema;
import red.vortx.bitfuzz.schema.api.SchemaBuilder;
import red.vortx.bitfuzz.schema.api.SchemaException;
import red.vortx.bitfuzz.schema.std.Types;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Timeout(60)
class MutationRunnerTest {

    private static final Schema UNCHECKED = SchemaBuilder.create()
            .field(BuggyTypes.UNCHECKED_STRING, "name")
            .build();

    private static FuzzResult run(final Schema schema, final int iterations, final long seed) {
        return new MutationRunner(schema, Generators.standard(), Mutators.standard(), new ByteShrinker())
                .run(iterations, seed);
    }

    @Test
    void standardTypesAreClean() {
        final Schema schema = SchemaBuilder.create()
                .field(Types.VAR_INT, "id")
                .field(Types.STRING, "name")
                .field(Types.list(Types.optional(Types.UUID)), "friends")
                .build();

        final FuzzResult result = run(schema, 10_000, 42);

        assertThat(result.isClean()).as(result.report()).isTrue();
    }

    @Test
    void findsAMissingLengthCheck() {
        final FuzzResult result = run(UNCHECKED, 5_000, 42);

        assertThat(result.failures())
                .as(result.report())
                .anySatisfy(failure -> assertThat(failure.failure()).isInstanceOf(NegativeArraySizeException.class));
    }

    @Test
    void shrinksTheMissingLengthCheckToFourBytes() {
        final FuzzFailure failure = run(UNCHECKED, 5_000, 42).failures().stream()
                .filter(candidate -> candidate.failure() instanceof NegativeArraySizeException)
                .findFirst()
                .orElseThrow();

        final byte[] shrunk = failure.shrunkInput();

        assertThat(shrunk).as("a negative length, with every other byte zeroed").hasSize(4);
        assertThat(shrunk[0]).isNegative();
        assertThat(shrunk[1]).isZero();
        assertThat(shrunk[2]).isZero();
        assertThat(shrunk[3]).isZero();
        assertThat(failure.input().length).isGreaterThanOrEqualTo(shrunk.length);
    }

    @Test
    void shrunkInputStillReproducesTheBug() {
        for (final FuzzFailure failure : run(UNCHECKED, 5_000, 42).failures()) {
            assertThatThrownBy(() -> Bytes.decode(UNCHECKED, failure.shrunkInput()))
                    .isNotInstanceOf(SchemaException.class)
                    .isInstanceOf(failure.failure().getClass());
        }
    }

    @Test
    void reportShowsSeedAndHex() {
        final FuzzResult result = run(UNCHECKED, 5_000, 42);

        assertThat(result.report())
                .contains("seed=")
                .contains("shrunk (4 bytes)")
                .doesNotContain("[B@");
    }

    @Test
    void sameSeedFindsSameFailures() {
        final List<Long> first = run(UNCHECKED, 2_000, 7).failures().stream().map(FuzzFailure::seed).toList();
        final List<Long> second = run(UNCHECKED, 2_000, 7).failures().stream().map(FuzzFailure::seed).toList();

        assertThat(first).isNotEmpty().isEqualTo(second);
    }

    @Test
    void stopsAfterMaxFailures() {
        final FuzzResult result = run(UNCHECKED, 1_000_000, 42);

        assertThat(result.failures()).hasSize(MutationRunner.DEFAULT_MAX_FAILURES);
        assertThat(result.iterations()).isLessThan(1_000_000);
    }

}
