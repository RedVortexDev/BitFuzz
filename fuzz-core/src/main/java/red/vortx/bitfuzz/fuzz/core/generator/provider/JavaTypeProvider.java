package red.vortx.bitfuzz.fuzz.core.generator.provider;

import red.vortx.bitfuzz.fuzz.api.Generator;
import red.vortx.bitfuzz.fuzz.api.GeneratorLookup;
import red.vortx.bitfuzz.fuzz.api.GeneratorProvider;
import red.vortx.bitfuzz.fuzz.core.generator.SingleGenerators;
import red.vortx.bitfuzz.fuzz.core.generator.impl.StringGenerator;
import red.vortx.bitfuzz.schema.api.field.type.Type;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/// Provides generators for single-value types by their [Java type][Type#javaType()].
///
/// @param generators the generator for each Java type
public record JavaTypeProvider(Map<Class<?>, Generator<?>> generators) implements GeneratorProvider {

    private static final JavaTypeProvider STANDARD = new JavaTypeProvider(Map.of(
            Byte.class, SingleGenerators.BYTE,
            Short.class, SingleGenerators.SHORT,
            Integer.class, SingleGenerators.INT,
            Long.class, SingleGenerators.LONG,
            Float.class, SingleGenerators.FLOAT,
            Double.class, SingleGenerators.DOUBLE,
            Boolean.class, SingleGenerators.BOOLEAN,
            UUID.class, SingleGenerators.UUID_VALUE,
            String.class, new StringGenerator()
    ));

    public JavaTypeProvider(final Map<Class<?>, Generator<?>> generators) {
        this.generators = Map.copyOf(generators);
    }

    /// {@return a provider for numbers, booleans, strings, and UUIDs}
    public static JavaTypeProvider standard() {
        return STANDARD;
    }

    @Override
    public <T> Optional<Generator<T>> create(final Type<T> type, final GeneratorLookup lookup) {
        //noinspection unchecked
        return Optional.ofNullable((Generator<T>) this.generators.get(type.javaType()));
    }

}
