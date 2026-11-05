package red.vortx.bitfuzz.fuzz.core.generator.provider;

import red.vortx.bitfuzz.fuzz.api.Generator;
import red.vortx.bitfuzz.fuzz.api.GeneratorLookup;
import red.vortx.bitfuzz.fuzz.api.GeneratorProvider;
import red.vortx.bitfuzz.fuzz.core.generator.impl.FixedStringGenerator;
import red.vortx.bitfuzz.fuzz.core.generator.impl.ListGenerator;
import red.vortx.bitfuzz.fuzz.core.generator.impl.MapGenerator;
import red.vortx.bitfuzz.fuzz.core.generator.impl.OptionalGenerator;
import red.vortx.bitfuzz.schema.api.field.type.Type;
import red.vortx.bitfuzz.schema.std.FixedStringType;
import red.vortx.bitfuzz.schema.std.ListType;
import red.vortx.bitfuzz.schema.std.MapType;
import red.vortx.bitfuzz.schema.std.OptionalType;

import java.util.Optional;

/// Provides generators for the standard library container types.
public record ContainerProvider() implements GeneratorProvider {

    @Override
    public <T> Optional<Generator<T>> create(final Type<T> type, final GeneratorLookup lookup) {
        final Generator<?> generator = switch (type) {
            case FixedStringType(final int byteLength) -> new FixedStringGenerator(byteLength);
            case ListType<?>(final Type<?> element, _) -> new ListGenerator<>(lookup.ofType(element));
            case MapType<?, ?>(final Type<?> key, final Type<?> value, _) ->
                    new MapGenerator<>(lookup.ofType(key), lookup.ofType(value));
            case OptionalType<?>(final Type<?> value) -> new OptionalGenerator<>(lookup.ofType(value));
            default -> null;
        };

        //noinspection unchecked
        return Optional.ofNullable((Generator<T>) generator);
    }

}
