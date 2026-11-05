package red.vortx.bitfuzz.fuzz.api;

import java.util.random.RandomGenerator;

/// Turns valid encoded bytes into slightly broken ones.
public interface Mutator {

    /// Returns a mutated copy of the input. The input must not be modified.
    ///
    /// @param input  the bytes to mutate
    /// @param random the source of randomness
    /// @return the mutated bytes
    byte[] mutate(byte[] input, RandomGenerator random);

    /// {@return a short name for this mutator}
    default String name() {
        return this.getClass().getSimpleName();
    }

}
