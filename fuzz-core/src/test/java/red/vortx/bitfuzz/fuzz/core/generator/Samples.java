package red.vortx.bitfuzz.fuzz.core.generator;

import red.vortx.bitfuzz.fuzz.api.Generator;
import red.vortx.bitfuzz.schema.api.Schema;
import red.vortx.bitfuzz.schema.api.SchemaBuilder;
import red.vortx.bitfuzz.schema.api.field.type.Type;
import red.vortx.bitfuzz.schema.api.field.type.TypeSpec;

import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

enum Samples {
    ;

    static final long SEED = 42;
    static final int COUNT = 2_000;

    static RandomGenerator random() {
        return random(SEED);
    }

    static RandomGenerator random(final long seed) {
        return RandomGeneratorFactory.of("L64X128MixRandom").create(seed);
    }

    static <T> List<T> sample(final Generator<T> generator) {
        return sample(generator, random(), COUNT);
    }

    static <T> List<T> sample(final Generator<T> generator, final RandomGenerator random, final int count) {
        final List<T> values = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            values.add(generator.generate(random));
        }
        return values;
    }

    static Schema single(final TypeSpec<?> type) {
        return SchemaBuilder.create().field(type, "value").build();
    }

    static <T> Type<T> resolve(final TypeSpec<T> type) {
        //noinspection unchecked
        return (Type<T>) single(type).fields().getFirst().type();
    }

}
