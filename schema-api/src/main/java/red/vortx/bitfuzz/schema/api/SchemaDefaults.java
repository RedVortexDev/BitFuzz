package red.vortx.bitfuzz.schema.api;

import red.vortx.bitfuzz.schema.api.field.type.Type;
import red.vortx.bitfuzz.schema.api.field.type.TypeSpec;

/// Default values used for building, serializing, and deserializing schemas.
public interface SchemaDefaults {

    /// {@return the schema's endianness}
    Endianness endianness();

    /// {@return the type used to represent the length of variable-length fields}
    Type<Integer> lengthType();

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

        /// Builds [SchemaDefaults] from this mutable version.
        ///
        /// @return an immutable [SchemaDefaults]
        SchemaDefaults build();

    }

}
