package red.vortx.bitfuzz.fuzz.core.generator.impl;

import red.vortx.bitfuzz.fuzz.api.Generator;
import red.vortx.bitfuzz.fuzz.core.generator.Sizes;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.random.RandomGenerator;

/// Generates lists of [small][Sizes#collection(RandomGenerator)] random sizes.
///
/// @param element the generator for the elements
/// @param <T>     the type of the elements
public record ListGenerator<T>(Generator<T> element) implements Generator<List<T>> {

    public ListGenerator {
        Objects.requireNonNull(element, "element");
    }

    @Override
    public List<T> generate(final RandomGenerator random) {
        final int size = Sizes.collection(random);
        final List<T> list = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            list.add(this.element.generate(random));
        }

        return list;
    }

}
