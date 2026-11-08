// This file is part of BitFuzz.
// SPDX-FileCopyrightText: 2026 RedVortexDev
// SPDX-License-Identifier: Apache-2.0

package red.vortx.bitfuzz.schema.std.type;

import red.vortx.bitfuzz.schema.api.SchemaException;
import red.vortx.bitfuzz.schema.api.field.type.EndiannessTypeSpec;
import red.vortx.bitfuzz.schema.api.field.type.Primitives;
import red.vortx.bitfuzz.schema.api.field.type.SimpleType;
import red.vortx.bitfuzz.schema.api.field.type.Type;
import red.vortx.bitfuzz.schema.api.field.type.TypeSpec;
import red.vortx.bitfuzz.schema.std.key.SchemaStandardKeys;
import red.vortx.bitfuzz.schema.std.type.enumeration.NamedEnumType;
import red.vortx.bitfuzz.schema.std.type.enumeration.OrdinalEnumType;
import red.vortx.bitfuzz.schema.std.type.record.RecordMapper;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/// Standard types that can be used in a schema. Includes endianness aware types and extended types.
public enum Types {
    ;

    // Primitive + endianness aware

    /// A byte.
    public static final Type<Byte> BYTE = Primitives.BYTE;
    /// A big-endian short.
    public static final Type<Short> SHORT_BE = Primitives.SHORT;
    /// A little-endian short.
    public static final Type<Short> SHORT_LE = Primitives.SHORT_LE;
    /// A short. Follows schema default endianness.
    public static final TypeSpec<Short> SHORT = new EndiannessTypeSpec<>(SHORT_BE, SHORT_LE);
    /// A big-endian integer.
    public static final Type<Integer> INT_BE = Primitives.INT;
    /// A little-endian integer.
    public static final Type<Integer> INT_LE = Primitives.INT_LE;
    /// An integer. Follows schema default endianness.
    public static final TypeSpec<Integer> INT = new EndiannessTypeSpec<>(INT_BE, INT_LE);
    /// A big-endian long.
    public static final Type<Long> LONG_BE = Primitives.LONG;
    /// A little-endian long.
    public static final Type<Long> LONG_LE = Primitives.LONG_LE;
    /// A long. Follows schema default endianness.
    public static final TypeSpec<Long> LONG = new EndiannessTypeSpec<>(LONG_BE, LONG_LE);
    /// A big-endian float.
    public static final Type<Float> FLOAT_BE = Primitives.FLOAT;
    /// A little-endian float.
    public static final Type<Float> FLOAT_LE = Primitives.FLOAT_LE;
    /// A float. Follows schema default endianness.
    public static final TypeSpec<Float> FLOAT = new EndiannessTypeSpec<>(FLOAT_BE, FLOAT_LE);
    /// A big-endian double.
    public static final Type<Double> DOUBLE_BE = Primitives.DOUBLE;
    /// A little-endian double.
    public static final Type<Double> DOUBLE_LE = Primitives.DOUBLE_LE;
    /// A double. Follows schema default endianness.
    public static final TypeSpec<Double> DOUBLE = new EndiannessTypeSpec<>(DOUBLE_BE, DOUBLE_LE);

    // Base java

    /// A boolean. Encoded as a single byte, `0` false, `1` true. Any other value is rejected.
    public static final Type<Boolean> BOOL = new BooleanType();

    /// A UUID. Encoded as 2 longs, does not follow schema endianness as the UUID byte order is fixed by the standard.
    public static final Type<UUID> UUID = new SimpleType<>(UUID.class,
            (writer, value) -> {
                writer.writeLong(value.getMostSignificantBits());
                writer.writeLong(value.getLeastSignificantBits());
            },
            reader -> new UUID(reader.readLong(), reader.readLong())
    );

    /// A fixed-length UTF-8 string. No length prefix and not padded.
    ///
    /// @param byteLength the number of bytes
    public static Type<String> fixedString(final int byteLength) {
        return new FixedStringType(byteLength);
    }

    // Variable-length

    /// A variable-length 32-bit integer. 7 bits per byte, least significant group first, with the high bit set on every
    /// byte except the last. Negative values always take 5 bytes.
    ///
    /// Reading rejects encodings longer than 5 bytes and a 5th byte that carries bits beyond 32. Overlong but otherwise
    /// valid encodings (e.g. `80 00` for 0) are accepted.
    public static final Type<Integer> VAR_INT = new VarIntType();
    /// A variable-length 64-bit integer. Encoded like [VarIntType] but up to 10 bytes. Negative values always take
    /// 10 bytes.
    ///
    /// Reading rejects encodings longer than 10 bytes and a 10th byte that carries bits beyond 64. Overlong but
    /// otherwise valid encodings are accepted.
    public static final Type<Long> VAR_LONG = new VarLongType();

    /// The schema's [length type][SchemaStandardKeys#LENGTH_TYPE], a [varint][#VAR_INT] by default.
    public static final TypeSpec<Integer> LENGTH = defaults ->
            defaults.get(SchemaStandardKeys.LENGTH_TYPE).resolve(defaults);

    /// A UTF-8 string. Prefixed with its length in bytes. Length prefix type is the schema's [length type][#LENGTH].
    ///
    /// Strict encoding and decoding, unpaired surrogates when writing and malformed UTF-8 when reading throw
    /// [SchemaException].
    public static final TypeSpec<String> STRING = defaults -> new StringType(LENGTH.resolve(defaults));

    /// A UTF-8 string. Prefixed with its length in bytes.
    ///
    /// Strict encoding and decoding, unpaired surrogates when writing and malformed UTF-8 when reading throw
    /// [SchemaException].
    ///
    /// @param lengthType the type of the length prefix
    public static TypeSpec<String> stringOf(final TypeSpec<Integer> lengthType) {
        Objects.requireNonNull(lengthType, "lengthType");

        return defaults -> new StringType(lengthType.resolve(defaults));
    }

    // Tagged

    /// The schema's [tag type][SchemaStandardKeys#TAG_TYPE], a [varint][#VAR_INT] by default.
    public static final TypeSpec<Integer> TAG = defaults ->
            defaults.get(SchemaStandardKeys.TAG_TYPE).resolve(defaults);

    /// An enum constant. Encoded with schema's [enum encoding][SchemaStandardKeys#ENUM_ENCODING].
    ///
    /// @param enumClass the enum class
    /// @param <E>       the enum type
    public static <E extends Enum<E>> TypeSpec<E> enumOf(final Class<E> enumClass) {
        Objects.requireNonNull(enumClass, "enumClass");

        return defaults -> switch (defaults.get(SchemaStandardKeys.ENUM_ENCODING)) {
            case ORDINAL -> enumOrdinalOf(enumClass).resolve(defaults);
            case NAME -> enumNameOf(enumClass).resolve(defaults);
        };
    }

    /// An enum constant written as its ordinal, as the schema's [tag type][#TAG].
    ///
    /// @param enumClass the enum class
    /// @param <E>       the enum type
    public static <E extends Enum<E>> TypeSpec<E> enumOrdinalOf(final Class<E> enumClass) {
        return enumOrdinalOf(enumClass, TAG);
    }

    /// An enum constant written as its ordinal.
    ///
    /// @param enumClass the enum class
    /// @param tagType   the type of the ordinal
    /// @param <E>       the enum type
    public static <E extends Enum<E>> TypeSpec<E> enumOrdinalOf(final Class<E> enumClass, final TypeSpec<Integer> tagType) {
        Objects.requireNonNull(enumClass, "enumClass");
        Objects.requireNonNull(tagType, "tagType");

        return defaults -> new OrdinalEnumType<>(enumClass, tagType.resolve(defaults));
    }

    /// An enum constant written as its name, as a [string][#STRING].
    ///
    /// @param enumClass the enum class
    /// @param <E>       the enum type
    public static <E extends Enum<E>> TypeSpec<E> enumNameOf(final Class<E> enumClass) {
        return enumNameOf(enumClass, STRING);
    }

    /// An enum constant written as its name.
    ///
    /// @param enumClass the enum class
    /// @param nameType  the type of the name
    /// @param <E>       the enum type
    public static <E extends Enum<E>> TypeSpec<E> enumNameOf(final Class<E> enumClass, final TypeSpec<String> nameType) {
        Objects.requireNonNull(enumClass, "enumClass");
        Objects.requireNonNull(nameType, "nameType");

        return defaults -> new NamedEnumType<>(enumClass, nameType.resolve(defaults));
    }

    // Collections

    /// A list. An element count followed by each element in iteration order. Reading returns a list that keeps the
    /// order of the bytes. Length prefix type is the schema's [length type][#LENGTH].
    ///
    /// @param elementType the type of the elements
    /// @param <T>         the type of the elements
    public static <T> TypeSpec<List<T>> listOf(final TypeSpec<T> elementType) {
        return listOf(elementType, LENGTH);
    }

    /// A list. An element count followed by each element in iteration order. Reading returns a list that keeps the
    /// order of the bytes.
    ///
    /// @param elementType the type of the elements
    /// @param lengthType  the type of the count prefix
    /// @param <T>         the type of the elements
    public static <T> TypeSpec<List<T>> listOf(final TypeSpec<T> elementType, final TypeSpec<Integer> lengthType) {
        Objects.requireNonNull(elementType, "elementType");
        Objects.requireNonNull(lengthType, "lengthType");

        return defaults -> new ListType<>(
                elementType.resolve(defaults),
                lengthType.resolve(defaults)
        );
    }

    /// A map. An entry count followed by each key and value in iteration order. Reading returns a map that keeps the
    /// order of the bytes and rejects duplicate keys. Length prefix type is the schema's [length type][#LENGTH].
    ///
    /// @param keyType   the type of the keys
    /// @param valueType the type of the values
    /// @param <K>       the type of the keys
    /// @param <V>       the type of the values
    public static <K, V> TypeSpec<Map<K, V>> mapOf(final TypeSpec<K> keyType, final TypeSpec<V> valueType) {
        return mapOf(keyType, valueType, LENGTH);
    }

    /// A map. An entry count followed by each key and value in iteration order. Reading returns a map that keeps the
    /// order of the bytes and rejects duplicate keys.
    ///
    /// @param keyType    the type of the keys
    /// @param valueType  the type of the values
    /// @param lengthType the type of the count prefix
    /// @param <K>        the type of the keys
    /// @param <V>        the type of the values
    public static <K, V> TypeSpec<Map<K, V>> mapOf(final TypeSpec<K> keyType, final TypeSpec<V> valueType, final TypeSpec<Integer> lengthType) {
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

    /// An optional value. A presence byte (`0` empty, `1` present) followed by the value when present.
    /// Any other presence byte is rejected.
    ///
    /// @param valueType the type of the value
    /// @param <T>       the type of the value
    public static <T> TypeSpec<Optional<T>> optionalOf(final TypeSpec<T> valueType) {
        Objects.requireNonNull(valueType, "valueType");

        return defaults -> new OptionalType<>(valueType.resolve(defaults));
    }

    // Records

    /// A record. Each component in declaration order, typed as described in [RecordMapper].
    ///
    /// @param recordClass the record class
    /// @param <R>         the record type
    /// @return a spec for the record
    public static <R extends Record> TypeSpec<R> recordOf(final Class<R> recordClass) {
        return RecordMapper.spec(recordClass);
    }

}
