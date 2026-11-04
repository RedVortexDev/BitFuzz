package red.vortx.bitfuzz.fuzz.api;

import red.vortx.bitfuzz.schema.api.field.type.Type;

import java.util.Optional;

/// Creates [generators][Generator] for types.
public interface GeneratorProvider {

    /// Creates a generator for the given type.
    ///
    /// @param type   the resolved type to generate values for
    /// @param lookup used to get generators for nested types
    /// @param <T>    the type of the values
    /// @return a generator or empty if this provider does not handle the type
    <T> Optional<Generator<T>> create(Type<T> type, GeneratorLookup lookup);

}
