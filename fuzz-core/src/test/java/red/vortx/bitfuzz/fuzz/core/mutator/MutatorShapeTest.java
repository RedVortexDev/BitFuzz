package red.vortx.bitfuzz.fuzz.core.mutator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import red.vortx.bitfuzz.fuzz.api.Mutator;

import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.random.RandomGenerator;

import static org.assertj.core.api.Assertions.assertThat;
import static red.vortx.bitfuzz.fuzz.core.mutator.MutatorContractTest.random;

@Timeout(10)
class MutatorShapeTest {

    private static final int RUNS = 2_000;

    private static void forEachMutation(final Mutator mutator, final BiConsumer<byte[], byte[]> check) {
        final RandomGenerator random = random(7);
        for (int i = 0; i < RUNS; i++) {
            final byte[] input = new byte[random.nextInt(1, 64)];
            random.nextBytes(input);
            check.accept(input, mutator.mutate(input, random));
        }
    }

    private static int differingBytes(final byte[] first, final byte[] second) {
        int count = 0;
        for (int i = 0; i < first.length; i++) {
            if (first[i] != second[i]) {
                count++;
            }
        }
        return count;
    }

    /// {@return whether `longer` is `shorter` with one contiguous run of bytes inserted}
    private static boolean isInsertionOf(final byte[] shorter, final byte[] longer) {
        final int extra = longer.length - shorter.length;
        if (extra <= 0) {
            return false;
        }
        int prefix = 0;
        while (prefix < shorter.length && shorter[prefix] == longer[prefix]) {
            prefix++;
        }
        return Arrays.equals(shorter, prefix, shorter.length, longer, prefix + extra, longer.length);
    }

    @Test
    void bitFlipChangesExactlyOneBit() {
        forEachMutation(new BitFlipMutator(), (input, output) -> {
            assertThat(output).hasSameSizeAs(input);
            int flippedBits = 0;
            for (int i = 0; i < input.length; i++) {
                flippedBits += Integer.bitCount((input[i] ^ output[i]) & 0xFF);
            }
            assertThat(flippedBits).isEqualTo(1);
        });
    }

    @Test
    void interestingByteChangesAtMostOneByte() {
        forEachMutation(new InterestingByteMutator(), (input, output) -> {
            assertThat(output).hasSameSizeAs(input);
            assertThat(differingBytes(input, output)).isLessThanOrEqualTo(1);
        });
    }

    @Test
    void interestingIntChangesAtMostFourConsecutiveBytes() {
        forEachMutation(new InterestingIntMutator(), (input, output) -> {
            assertThat(output).hasSameSizeAs(input);
            int first = -1;
            int last = -1;
            for (int i = 0; i < input.length; i++) {
                if (input[i] != output[i]) {
                    first = first == -1 ? i : first;
                    last = i;
                }
            }
            if (first != -1) {
                assertThat(last - first).isLessThan(Integer.BYTES);
            }
        });
    }

    @Test
    void interestingIntLeavesShortInputsAlone() {
        final RandomGenerator random = random(8);
        for (int length = 0; length < Integer.BYTES; length++) {
            final byte[] input = new byte[length];
            assertThat(new InterestingIntMutator().mutate(input, random)).containsExactly(input);
        }
    }

    @Test
    void truncateKeepsAPrefix() {
        forEachMutation(new TruncateMutator(), (input, output) -> {
            assertThat(output.length).isLessThanOrEqualTo(input.length);
            assertThat(output).containsExactly(Arrays.copyOf(input, output.length));
        });
    }

    @Test
    void deleteRangeRemovesOneContiguousRange() {
        forEachMutation(new DeleteRangeMutator(), (input, output) -> {
            assertThat(output.length).isLessThan(input.length);
            assertThat(isInsertionOf(output, input)).isTrue();
        });
    }

    @Test
    void insertRandomAddsOneContiguousRunOfBoundedSize() {
        forEachMutation(new InsertRandomMutator(), (input, output) -> {
            assertThat(output.length - input.length).isBetween(1, InsertRandomMutator.MAX_INSERTED_BYTES);
            assertThat(isInsertionOf(input, output)).isTrue();
        });
    }

    @Test
    void insertRandomWorksOnEmptyInput() {
        final byte[] output = new InsertRandomMutator().mutate(new byte[0], random(9));

        assertThat(output.length).isBetween(1, InsertRandomMutator.MAX_INSERTED_BYTES);
    }

    @Test
    void duplicateChunkInsertsACopyOfExistingBytes() {
        forEachMutation(new DuplicateChunkMutator(), (input, output) -> {
            final int chunkSize = output.length - input.length;
            assertThat(chunkSize).isBetween(1, DuplicateChunkMutator.MAX_CHUNK_SIZE);
            assertThat(isInsertionOf(input, output)).isTrue();

            boolean chunkComesFromInput = false;
            for (int position = 0; position + chunkSize <= output.length && !chunkComesFromInput; position++) {
                final byte[] withoutChunk = new byte[input.length];
                System.arraycopy(output, 0, withoutChunk, 0, position);
                System.arraycopy(output, position + chunkSize, withoutChunk, position, output.length - position - chunkSize);
                if (Arrays.equals(withoutChunk, input)) {
                    final byte[] chunk = Arrays.copyOfRange(output, position, position + chunkSize);
                    for (int start = 0; start + chunkSize <= input.length; start++) {
                        if (Arrays.equals(chunk, 0, chunkSize, input, start, start + chunkSize)) {
                            chunkComesFromInput = true;
                            break;
                        }
                    }
                }
            }
            assertThat(chunkComesFromInput).isTrue();
        });
    }

}
