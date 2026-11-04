package red.vortx.bitfuzz.fuzz.core.mutator;

import red.vortx.bitfuzz.fuzz.api.Mutator;

import java.util.random.RandomGenerator;

/// Cuts the input off at a random length.
///
/// Checks: underflow
public class TruncateMutator implements Mutator {

    @Override
    public byte[] mutate(final byte[] input, final RandomGenerator random) {
        final int newSize = random.nextInt(input.length + 1);

        final byte[] copy = new byte[newSize];
        System.arraycopy(input, 0, copy, 0, newSize);

        return copy;
    }

}
