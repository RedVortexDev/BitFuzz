package red.vortx.bitfuzz.fuzz.core.generator;

import red.vortx.bitfuzz.fuzz.api.Generator;
import red.vortx.bitfuzz.fuzz.api.GeneratorLookup;
import red.vortx.bitfuzz.fuzz.api.GeneratorProvider;
import red.vortx.bitfuzz.fuzz.core.generator.provider.ContainerProvider;
import red.vortx.bitfuzz.fuzz.core.generator.provider.JavaTypeProvider;
import red.vortx.bitfuzz.fuzz.core.generator.provider.RecordProvider;
import red.vortx.bitfuzz.schema.api.field.type.Type;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/// Holds a list of [GeneratorProvider]s and checks them in order for a generator for a type.
///
/// @param providers the providers to check, in order
public record Generators(List<GeneratorProvider> providers) implements GeneratorLookup {

    private static final Generators STANDARD = new Generators(List.of(
            new ContainerProvider(),
            new RecordProvider(),
            JavaTypeProvider.standard()
    ));

    public Generators(final List<GeneratorProvider> providers) {
        this.providers = List.copyOf(providers);
    }

    /// {@return the generators for every schema-std type}
    public static Generators standard() {
        return STANDARD;
    }

    /// {@return a copy that checks the given provider before the others}
    ///
    /// @param provider the provider to check first
    public Generators with(final GeneratorProvider provider) {
        final List<GeneratorProvider> combined = new ArrayList<>(this.providers.size() + 1);

        combined.add(provider);
        combined.addAll(this.providers);

        return new Generators(combined);
    }

    @Override
    public <T> Generator<T> ofType(final Type<T> type) {
        for (final GeneratorProvider provider : this.providers) {
            final Optional<Generator<T>> generator = provider.create(type, this);
            if (generator.isPresent()) {
                return generator.get();
            }
        }

        throw new IllegalArgumentException("No generator for type %s (Java type %s)"
                .formatted(type, type.javaType().getName())
        );
    }

}
