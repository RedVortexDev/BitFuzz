package red.vortx.bitfuzz.fuzz.core.generator.impl;

import red.vortx.bitfuzz.fuzz.api.Generator;

import java.util.List;
import java.util.Objects;
import java.util.random.RandomGenerator;

/// Picks evenly from edge values, small values, and values from the whole range.
///
/// @param edges edge values
/// @param small generator for small, realistic values
/// @param any   generator for values from the whole range
/// @param <T>   the type of the values
public record BiasedGenerator<T>(List<T> edges, Generator<T> small, Generator<T> any) implements Generator<T> {

    public BiasedGenerator(final List<T> edges, final Generator<T> small, final Generator<T> any) {
        if (edges.isEmpty()) {
            throw new IllegalArgumentException("Edge values must not be empty");
        }
        this.edges = List.copyOf(edges);
        this.small = Objects.requireNonNull(small, "small");
        this.any = Objects.requireNonNull(any, "any");
    }

    @Override
    public T generate(final RandomGenerator random) {
        return switch (random.nextInt(3)) {
            case 0 -> this.edges.get(random.nextInt(this.edges.size()));
            case 1 -> this.small.generate(random);
            default -> this.any.generate(random);
        };
    }

}
