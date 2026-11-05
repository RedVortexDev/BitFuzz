package red.vortx.bitfuzz.fuzz.core.generator.provider;

import red.vortx.bitfuzz.fuzz.api.Generator;
import red.vortx.bitfuzz.fuzz.api.GeneratorLookup;
import red.vortx.bitfuzz.fuzz.api.GeneratorProvider;
import red.vortx.bitfuzz.fuzz.core.generator.impl.RecordGenerator;
import red.vortx.bitfuzz.schema.api.field.type.Type;
import red.vortx.bitfuzz.schema.std.record.RecordType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/// Provides generators for [mapped records][RecordType].
public record RecordProvider() implements GeneratorProvider {

    @Override
    public <T> Optional<Generator<T>> create(final Type<T> type, final GeneratorLookup lookup) {
        if (!(type instanceof final RecordType<?> recordType)) {
            return Optional.empty();
        }

        final List<Generator<?>> components = new ArrayList<>(recordType.components().size());
        for (final RecordType.Component component : recordType.components()) {
            components.add(lookup.ofType(component.type()));
        }

        //noinspection unchecked
        return Optional.of((Generator<T>) new RecordGenerator<>(recordType, components));
    }

}
