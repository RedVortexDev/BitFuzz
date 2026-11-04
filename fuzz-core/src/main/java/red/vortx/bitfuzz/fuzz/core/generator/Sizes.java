package red.vortx.bitfuzz.fuzz.core.generator;

import java.util.random.RandomGenerator;

/// Sizes for generated collections.
public enum Sizes {
    ;

    public static final int MAX_COLLECTION_SIZE = 8;

    /// {@return a random size from 0 to [#MAX_COLLECTION_SIZE]}
    ///
    /// 0 comes up a quarter of the time.
    ///
    /// @param random the source of randomness
    public static int collection(final RandomGenerator random) {
        final boolean isZeroSize = random.nextInt(4) == 0;
        if (isZeroSize) {
            return 0;
        }

        return  random.nextInt(1, MAX_COLLECTION_SIZE + 1);
    }

}
