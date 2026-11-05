package red.vortx.bitfuzz.schema.std;

import red.vortx.bitfuzz.schema.api.field.type.EndiannessTypeSpec;
import red.vortx.bitfuzz.schema.api.field.type.SimpleType;
import red.vortx.bitfuzz.schema.api.field.type.Type;
import red.vortx.bitfuzz.schema.api.field.type.TypeSpec;
import red.vortx.bitfuzz.schema.core.Primitives;
import red.vortx.bitfuzz.schema.std.record.RecordMapper;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/// Standard types that can be used in a schema. Includes endianness aware types and extended types.
public enum Types {
    ;

    // Primitive + endianness aware

    public static final Type<Byte> BYTE = Primitives.BYTE;
    public static final Type<Short> SHORT_BE = Primitives.SHORT;
    public static final Type<Short> SHORT_LE = Primitives.SHORT_LE;
    public static final TypeSpec<Short> SHORT = new EndiannessTypeSpec<>(SHORT_BE, SHORT_LE);
    public static final Type<Integer> INT_BE = Primitives.INT;
    public static final Type<Integer> INT_LE = Primitives.INT_LE;
    public static final TypeSpec<Integer> INT = new EndiannessTypeSpec<>(INT_BE, INT_LE);
    public static final Type<Long> LONG_BE = Primitives.LONG;
    public static final Type<Long> LONG_LE = Primitives.LONG_LE;
    public static final TypeSpec<Long> LONG = new EndiannessTypeSpec<>(LONG_BE, LONG_LE);
    public static final Type<Float> FLOAT_BE = Primitives.FLOAT;
    public static final Type<Float> FLOAT_LE = Primitives.FLOAT_LE;
    public static final TypeSpec<Float> FLOAT = new EndiannessTypeSpec<>(FLOAT_BE, FLOAT_LE);
    public static final Type<Double> DOUBLE_BE = Primitives.DOUBLE;
    public static final Type<Double> DOUBLE_LE = Primitives.DOUBLE_LE;
    public static final TypeSpec<Double> DOUBLE = new EndiannessTypeSpec<>(DOUBLE_BE, DOUBLE_LE);

    // Base java

    public static final Type<Boolean> BOOL = new BooleanType();

    /// Does not follow schema endianness as the UUID byte order is fixed by the standard.
    public static final Type<UUID> UUID = new SimpleType<>(UUID.class,
            (writer, value) -> {
                writer.writeLong(value.getMostSignificantBits());
                writer.writeLong(value.getLeastSignificantBits());
            },
            reader -> new UUID(reader.readLong(), reader.readLong())
    );

    public static final TypeSpec<String> STRING = defaults -> new StringType(defaults.lengthType());

    public static TypeSpec<String> string(final TypeSpec<Integer> lengthType) {
        Objects.requireNonNull(lengthType, "lengthType");

        return defaults -> new StringType(lengthType.resolve(defaults));
    }

    /// A fixed-length UTF-8 string. No length prefix and not padded.
    ///
    /// @param byteLength the number of bytes
    public static Type<String> fixedString(final int byteLength) {
        return new FixedStringType(byteLength);
    }

    // Variable-length

    public static final Type<Integer> VAR_INT = new VarIntType();
    public static final Type<Long> VAR_LONG = new VarLongType();

    // Collections

    public static <T> TypeSpec<List<T>> list(final TypeSpec<T> elementType) {
        Objects.requireNonNull(elementType, "elementType");

        return defaults -> new ListType<>(
                elementType.resolve(defaults),
                defaults.lengthType()
        );
    }

    public static <T> TypeSpec<List<T>> list(final TypeSpec<T> elementType, final TypeSpec<Integer> lengthType) {
        Objects.requireNonNull(elementType, "elementType");
        Objects.requireNonNull(lengthType, "lengthType");

        return defaults -> new ListType<>(
                elementType.resolve(defaults),
                lengthType.resolve(defaults)
        );
    }

    public static <K, V> TypeSpec<Map<K, V>> map(final TypeSpec<K> keyType, final TypeSpec<V> valueType) {
        Objects.requireNonNull(keyType, "keyType");
        Objects.requireNonNull(valueType, "valueType");

        return defaults -> new MapType<>(
                keyType.resolve(defaults),
                valueType.resolve(defaults),
                defaults.lengthType()
        );
    }

    public static <K, V> TypeSpec<Map<K, V>> map(final TypeSpec<K> keyType, final TypeSpec<V> valueType, final TypeSpec<Integer> lengthType) {
        Objects.requireNonNull(keyType, "keyType");
        Objects.requireNonNull(valueType, "valueType");
        Objects.requireNonNull(lengthType, "lengthType");

        return defaults -> new MapType<>(
                keyType.resolve(defaults),
                valueType.resolve(defaults),
                lengthType.resolve(defaults)
        );
    }

    // Extras

    public static <T> TypeSpec<Optional<T>> optional(final TypeSpec<T> valueType) {
        Objects.requireNonNull(valueType, "valueType");

        return defaults -> new OptionalType<>(valueType.resolve(defaults));
    }

    // Records

    /// A record. Each component in declaration order, typed as described in [RecordMapper].
    ///
    /// @param recordClass the record class
    /// @param <R>         the record type
    /// @return a spec for the record
    public static <R extends Record> TypeSpec<R> record(final Class<R> recordClass) {
        return RecordMapper.spec(recordClass);
    }

}
