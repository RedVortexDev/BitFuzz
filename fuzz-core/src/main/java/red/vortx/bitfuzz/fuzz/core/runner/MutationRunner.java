// This file is part of BitFuzz.
// SPDX-FileCopyrightText: 2026 RedVortexDev
// SPDX-License-Identifier: Apache-2.0

package red.vortx.bitfuzz.fuzz.core.runner;

import red.vortx.bitfuzz.fuzz.api.FuzzFailure;
import red.vortx.bitfuzz.fuzz.api.FuzzResult;
import red.vortx.bitfuzz.fuzz.api.FuzzRunner;
import red.vortx.bitfuzz.fuzz.api.GeneratorLookup;
import red.vortx.bitfuzz.fuzz.api.Mutator;
import red.vortx.bitfuzz.fuzz.api.Shrinker;
import red.vortx.bitfuzz.fuzz.core.Bytes;
import red.vortx.bitfuzz.fuzz.core.generator.impl.SchemaGenerator;
import red.vortx.bitfuzz.schema.api.Schema;
import red.vortx.bitfuzz.schema.api.SchemaException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.random.RandomGenerator;

/// Checks that decoding damaged bytes never crashes.
/// Each case encodes generated values, applies random mutations, and decodes the result.
/// Decoding must either succeed or throw [SchemaException]; anything else is considered a bug.
///
/// @param schema      the schema under test
/// @param generator   the generator for the valid values that get mutated
/// @param mutators    the mutators to pick from
/// @param shrinker    the shrinker for failing inputs
/// @param maxFailures the number of failures after which the run stops early
public record MutationRunner(Schema schema, SchemaGenerator generator, List<Mutator> mutators, Shrinker shrinker,
                             int maxFailures) implements FuzzRunner {

    public static final int MAX_MUTATIONS = 3;
    public static final int DEFAULT_MAX_FAILURES = 10;

    public MutationRunner(final Schema schema, final SchemaGenerator generator, final List<Mutator> mutators, final Shrinker shrinker, final int maxFailures) {
        if (mutators.isEmpty()) {
            throw new IllegalArgumentException("At least one mutator is required");
        }
        if (maxFailures < 1) {
            throw new IllegalArgumentException("maxFailures must be at least 1");
        }
        this.schema = Objects.requireNonNull(schema, "schema");
        this.generator = Objects.requireNonNull(generator, "generator");
        this.mutators = List.copyOf(mutators);
        this.shrinker = Objects.requireNonNull(shrinker, "shrinker");
        this.maxFailures = maxFailures;
    }

    /// Creates a runner that generates values with the given lookup.
    ///
    /// @param schema   the schema under test
    /// @param lookup   where to find generators for the field types
    /// @param mutators the mutators to pick from
    /// @param shrinker the shrinker for failing inputs
    /// @throws IllegalArgumentException if a field's type has no generator
    public MutationRunner(final Schema schema, final GeneratorLookup lookup, final List<Mutator> mutators, final Shrinker shrinker) {
        this(schema, SchemaGenerator.of(schema, lookup), mutators, shrinker, DEFAULT_MAX_FAILURES);
    }

    @Override
    public FuzzResult run(final int iterations, final long seed) {
        final CaseSeeds seeds = new CaseSeeds(seed);
        final List<FuzzFailure> failures = new ArrayList<>();

        int iteration;
        for (iteration = 0; iteration < iterations && failures.size() < this.maxFailures; iteration++) {
            final long caseSeed = seeds.next();
            final byte[] input = this.mutateInput(CaseSeeds.random(caseSeed));

            final Optional<Throwable> bug = this.decodeAndGetBug(input);
            if (bug.isPresent()) {
                final Class<? extends Throwable> type = bug.get().getClass();
                final byte[] shrunk = this.shrinker.shrink(input, candidate ->
                        this.decodeAndGetBug(candidate)
                                .filter(other -> other.getClass() == type)
                                .isPresent()
                );
                failures.add(new FuzzFailure(caseSeed, iteration, input, shrunk, bug.get()));
            }
        }

        return new FuzzResult(iteration, failures);
    }

    private byte[] mutateInput(final RandomGenerator random) {
        byte[] bytes = Bytes.encode(this.schema, this.generator.generate(random));

        final int mutations = random.nextInt(1, MAX_MUTATIONS + 1);
        for (int i = 0; i < mutations; i++) {
            bytes = this.mutators.get(random.nextInt(this.mutators.size())).mutate(bytes, random);
        }

        return bytes;
    }

    private Optional<Throwable> decodeAndGetBug(final byte[] input) {
        try {
            Bytes.decode(this.schema, input);
            return Optional.empty();
        } catch (final SchemaException e) {
            return Optional.empty();
        } catch (final Throwable t) {
            return Optional.of(t);
        }
    }

}
