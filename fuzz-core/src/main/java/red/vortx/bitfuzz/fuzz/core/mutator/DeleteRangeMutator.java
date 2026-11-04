package red.vortx.bitfuzz.fuzz.core.mutator;

import red.vortx.bitfuzz.fuzz.api.Mutator;

import java.util.random.RandomGenerator;

/// Removes a random range of bytes.
///
/// Checks: fields shifted out of place, missing fields
public class DeleteRangeMutator implements Mutator {

    @Override
    public byte[] mutate(final byte[] input, final RandomGenerator random) {
        if (input.length == 0) {
            return input.clone();
        }

        final int start = random.nextInt(input.length);
        final int end = random.nextInt(start + 1, input.length + 1);

        final int chunkSize = end - start;
        final byte[] copy = new byte[input.length - chunkSize];
        System.arraycopy(input, 0, copy, 0, start);
        System.arraycopy(input, end, copy, start, input.length - end);

        return copy;
    }

}
