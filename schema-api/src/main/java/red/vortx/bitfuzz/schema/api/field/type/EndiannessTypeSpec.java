package red.vortx.bitfuzz.schema.api.field.type;

import red.vortx.bitfuzz.schema.api.SchemaDefaults;

import java.util.Objects;

/// A type that depends on the schema's endianness.
///
/// @param bigEndianType    the type to use if the schema is big-endian
/// @param littleEndianType the type to use if the schema is little-endian
/// @param <T>              the type of the value
public record EndiannessTypeSpec<T>(Type<T> bigEndianType, Type<T> littleEndianType) implements TypeSpec<T> {

    public EndiannessTypeSpec {
        Objects.requireNonNull(bigEndianType, "bigEndianType");
        Objects.requireNonNull(littleEndianType, "littleEndianType");
    }

    @Override
    public Type<T> resolve(final SchemaDefaults defaults) {
        return defaults.endianness().isBigEndian() ? this.bigEndianType : this.littleEndianType;
    }

}
