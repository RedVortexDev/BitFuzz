package red.vortx.bitfuzz.schema.api.key;

import java.util.Objects;

/// An option key for schema operations.
///
/// @param name     the name of the key
/// @param fallback the default value
/// @param <T>      the type of the value
public record SchemaKey<T>(String name, T fallback) {

    public SchemaKey {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(fallback, "fallback");
    }

}
