package red.vortx.bitfuzz.fuzz.core;

import red.vortx.bitfuzz.fuzz.api.FuzzResult;
import red.vortx.bitfuzz.fuzz.core.generator.Generators;
import red.vortx.bitfuzz.fuzz.core.mutator.Mutators;
import red.vortx.bitfuzz.fuzz.core.runner.MutationRunner;
import red.vortx.bitfuzz.fuzz.core.runner.RoundTripRunner;
import red.vortx.bitfuzz.fuzz.core.shrink.ByteShrinker;
import red.vortx.bitfuzz.schema.api.Schema;

/// Entry point for fuzzing a schema with the standard generators, mutators, and shrinker.
///
/// For custom types or mutators, use [RoundTripRunner] and [MutationRunner] directly.
public enum Fuzz {
    ;

    /// Checks that random valid values survive encoding and decoding unchanged.
    ///
    /// @param schema     the schema under test
    /// @param iterations the maximum number of cases to try
    /// @param seed       the seed that makes the run reproducible
    /// @return the outcome of the run
    /// @throws IllegalArgumentException if a field's type has no standard generator
    public static FuzzResult roundTrip(final Schema schema, final int iterations, final long seed) {
        final RoundTripRunner runner = new RoundTripRunner(schema, Generators.standard());
        return runner.run(iterations, seed);
    }

    /// Checks that decoding mutated encodings either succeeds or is cleanly rejected, shrinking any crash found.
    ///
    /// @param schema     the schema under test
    /// @param iterations the maximum number of cases to try
    /// @param seed       the seed that makes the run reproducible
    /// @return the outcome of the run
    /// @throws IllegalArgumentException if a field's type has no standard generator
    public static FuzzResult mutate(final Schema schema, final int iterations, final long seed) {
        final MutationRunner runner = new MutationRunner(
                schema, Generators.standard(), Mutators.standard(), new ByteShrinker()
        );
        return runner.run(iterations, seed);
    }

}
