package red.vortx.bitfuzz.fuzz.core.generator.impl;

import red.vortx.bitfuzz.fuzz.api.Generator;
import red.vortx.bitfuzz.fuzz.api.GeneratorLookup;
import red.vortx.bitfuzz.schema.api.Schema;
import red.vortx.bitfuzz.schema.api.field.Field;

import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;

/// Generates one value per schema field.
///
/// @param fields the generator for each field
public record SchemaGenerator(List<Generator<?>> fields) implements Generator<List<Object>> {

    public SchemaGenerator(final List<Generator<?>> fields) {
        this.fields = List.copyOf(fields);
    }

    /// Creates a generator for a schema's fields.
    ///
    /// @param schema the schema to generate values for
    /// @param lookup where to find generators for the field types
    /// @return a generator for the schema's values
    /// @throws IllegalArgumentException if a field's type has no generator
    public static SchemaGenerator of(final Schema schema, final GeneratorLookup lookup) {
        final List<Generator<?>> generators = new ArrayList<>(schema.fields().size());

        for (final Field field : schema.fields()) {
            generators.add(lookup.ofType(field.type()));
        }

        return new SchemaGenerator(generators);
    }

    @Override
    public List<Object> generate(final RandomGenerator random) {
        final List<Object> values = new ArrayList<>(this.fields.size());

        for (final Generator<?> field : this.fields) {
            values.add(field.generate(random));
        }

        return values;
    }

}
