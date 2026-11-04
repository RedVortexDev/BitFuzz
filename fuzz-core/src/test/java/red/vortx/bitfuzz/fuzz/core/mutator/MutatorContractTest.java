package red.vortx.bitfuzz.fuzz.core.mutator;

import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import red.vortx.bitfuzz.fuzz.api.Mutator;

import java.util.Arrays;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(10)
class MutatorContractTest {

    private static final int RUNS = 2_000;

    static Stream<Mutator> mutators() {
        return Stream.of(
                new BitFlipMutator(),
                new InterestingByteMutator(),
                new InterestingIntMutator(),
                new TruncateMutator(),
                new DeleteRangeMutator(),
                new InsertRandomMutator(),
                new DuplicateChunkMutator()
        );
    }

    static RandomGenerator random(final long seed) {
        return RandomGeneratorFactory.of("L64X128MixRandom").create(seed);
    }

    static byte[] sampleInput(final RandomGenerator random) {
        final byte[] input = new byte[random.nextInt(0, 64)];
        random.nextBytes(input);
        return input;
    }

    @ParameterizedTest
    @MethodSource("mutators")
    void neverModifiesInput(final Mutator mutator) {
        final RandomGenerator random = random(1);
        for (int i = 0; i < RUNS; i++) {
            final byte[] input = sampleInput(random);
            final byte[] original = input.clone();

            mutator.mutate(input, random);

            assertThat(input).containsExactly(original);
        }
    }

    @ParameterizedTest
    @MethodSource("mutators")
    void neverReturnsTheSameArray(final Mutator mutator) {
        final RandomGenerator random = random(2);
        for (int i = 0; i < RUNS; i++) {
            final byte[] input = sampleInput(random);

            assertThat(mutator.mutate(input, random)).isNotSameAs(input);
        }
    }

    @ParameterizedTest
    @MethodSource("mutators")
    void handlesTinyInputs(final Mutator mutator) {
        final RandomGenerator random = random(3);
        for (int length = 0; length <= 5; length++) {
            for (int i = 0; i < 200; i++) {
                assertThat(mutator.mutate(new byte[length], random)).isNotNull();
            }
        }
    }

    @ParameterizedTest
    @MethodSource("mutators")
    void sameSeedGivesSameOutput(final Mutator mutator) {
        final byte[] input = sampleInput(random(4));
        final RandomGenerator first = random(5);
        final RandomGenerator second = random(5);

        for (int i = 0; i < 100; i++) {
            assertThat(mutator.mutate(input, first)).containsExactly(mutator.mutate(input, second));
        }
    }

    @ParameterizedTest
    @MethodSource("mutators")
    void usuallyChangesTheInput(final Mutator mutator) {
        final RandomGenerator random = random(6);
        int changed = 0;
        for (int i = 0; i < RUNS; i++) {
            final byte[] input = new byte[32];
            random.nextBytes(input);
            if (!Arrays.equals(input, mutator.mutate(input, random))) {
                changed++;
            }
        }

        assertThat(changed).isGreaterThan(RUNS / 2);
    }

    @ParameterizedTest
    @MethodSource("mutators")
    void hasAName(final Mutator mutator) {
        assertThat(mutator.name()).isNotBlank().endsWith("Mutator");
    }

}
