package red.vortx.bitfuzz.schema.api;

import red.vortx.bitfuzz.schema.api.field.type.Type;
import red.vortx.bitfuzz.schema.api.field.type.TypeSpec;

import java.util.Optional;

/// Default values used for building, serializing, and deserializing schemas.
public interface SchemaDefaults {

    /// {@return the schema's endianness}
    Endianness endianness();

    /// {@return the type used to represent the length of variable-length fields}
    Type<Integer> lengthType();

    /// Gets the type mapped records use for a Java type instead of the built-in one.
    ///
    /// Primitive classes and their wrappers are the same key.
    ///
    /// @param javaType the Java type class
    /// @param <T>      the Java type
    /// @return the type, or empty if none was set
    <T> Optional<TypeSpec<T>> recordType(Class<T> javaType);

    /// {@return a mutable copy of these defaults}
    Mutable toMutable();

    /// A mutable version of [SchemaDefaults].
    interface Mutable extends SchemaDefaults {

        /// Sets the endianness of the schema.
        ///
        /// @param endianness the endianness to set
        /// @return this mutable schema defaults
        Mutable endianness(Endianness endianness);

        /// Sets the type used to represent the length of variable-length fields.
        ///
        /// Resolved when the schema is built, so it follows the final [endianness][#endianness()].
        ///
        /// @param lengthType the length type to set
        /// @return this mutable schema defaults
        Mutable lengthType(TypeSpec<Integer> lengthType);

        /// Sets the type mapped records use for a Java type instead of the built-in one.
        ///
        /// Primitive classes and their wrappers are the same key, so `int.class` also covers [Integer].
        ///
        /// @param javaType the Java type class
        /// @param type     the type to use
        /// @param <T>      the Java type
        /// @return this mutable schema defaults
        <T> Mutable recordType(Class<T> javaType, TypeSpec<T> type);

        /// Builds [SchemaDefaults] from this mutable version.
        ///
        /// @return an immutable [SchemaDefaults]
        SchemaDefaults build();

    }

}
