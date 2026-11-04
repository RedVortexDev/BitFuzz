package red.vortx.bitfuzz.schema.api;

import red.vortx.bitfuzz.schema.api.field.type.TypeSpec;

import java.util.function.Consumer;

/// A builder for [schemas][Schema].
public interface SchemaBuilder {

    /// Creates a new schema builder using the [SchemaProvider] found at runtime.
    ///
    /// @return a new schema builder
    /// @throws IllegalStateException if no [SchemaProvider] implementation is available
    static SchemaBuilder create() {
        return SchemaProviders.PROVIDER.builder();
    }

    /// Adds a field to the schema being built.
    ///
    /// @param type the type of the field
    /// @param name the name of the field
    /// @return this builder
    SchemaBuilder field(TypeSpec<?> type, String name);

    /// Configures the defaults of the schema being built.
    ///
    /// @param defaults a consumer that accepts a mutable [SchemaDefaults]
    /// @return this builder
    SchemaBuilder defaults(Consumer<SchemaDefaults.Mutable> defaults);

    /// Builds the schema.
    ///
    /// @return the built schema
    Schema build();

}
