package red.vortx.bitfuzz.schema.api;

import red.vortx.bitfuzz.schema.api.key.SchemaKey;
import red.vortx.bitfuzz.schema.api.key.SchemaKeys;

import java.util.Objects;
import java.util.function.UnaryOperator;

/// Default values used for building, serializing, and deserializing schemas.
public interface SchemaDefaults {

    /// {@return the value set for the provided key}
    ///
    /// Built in keys can be found in [SchemaKeys], standard library ones are in `SchemaStandardKeys`.
    ///
    /// @param key the key to get the value of
    /// @param <T> the type of the value
    <T> T get(SchemaKey<T> key);

    /// {@return a mutable copy of these defaults}
    Mutable toMutable();

    /// A mutable version of [SchemaDefaults].
    interface Mutable extends SchemaDefaults {

        /// Replaces the value of a key.
        ///
        /// Built-in keys can be found in [SchemaKeys], standard library ones are in `SchemaStandardKeys`.
        ///
        /// @param key   the key to set
        /// @param value the value to set
        /// @param <T>   the type of the value
        /// @return this mutable schema defaults
        <T> Mutable set(SchemaKey<T> key, T value);

        /// Replaces the value of a key based on its current value, or its fallback if none was set.
        ///
        /// Built-in keys can be found in [SchemaKeys], standard library ones are in `SchemaStandardKeys`.
        ///
        /// @param key     the key to update
        /// @param updater creates the new value from the current one
        /// @param <T>     the type of the value
        /// @return this mutable schema defaults
        default <T> Mutable update(final SchemaKey<T> key, final UnaryOperator<T> updater) {
            Objects.requireNonNull(updater, "updater");

            return this.set(key, updater.apply(this.get(key)));
        }

        /// Builds [SchemaDefaults] from this mutable version.
        ///
        /// @return an immutable [SchemaDefaults]
        SchemaDefaults build();

    }

}
