package red.vortx.bitfuzz.schema.std;

import red.vortx.bitfuzz.buffer.ByteReader;
import red.vortx.bitfuzz.buffer.ByteWriter;
import red.vortx.bitfuzz.schema.api.SchemaException;
import red.vortx.bitfuzz.schema.api.field.type.CompositeType;
import red.vortx.bitfuzz.schema.api.field.type.Type;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/// A map. An entry count, then each key and value in iteration order. Reading keeps that order and rejects
/// duplicate keys.
///
/// Reading rejects a negative count or one larger than the remaining bytes.
///
/// @param keyType    the type of the keys
/// @param valueType  the type of the values
/// @param lengthType the type of the count prefix
/// @param <K>        the type of the keys
/// @param <V>        the type of the values
public record MapType<K, V>(Type<K> keyType, Type<V> valueType,
                            Type<Integer> lengthType) implements CompositeType<Map<K, V>> {

    public MapType {
        Objects.requireNonNull(keyType, "keyType");
        Objects.requireNonNull(valueType, "valueType");
        Objects.requireNonNull(lengthType, "lengthType");
    }

    @Override
    public void write(final ByteWriter output, final Map<K, V> value) {
        this.lengthType.write(output, value.size());
        for (final Map.Entry<K, V> entry : value.entrySet()) {
            this.keyType.write(output, entry.getKey());
            this.valueType.write(output, entry.getValue());
        }
    }

    @Override
    public Map<K, V> read(final ByteReader input) {
        final int length = this.lengthType.read(input);

        if (length < 0) {
            throw new SchemaException("Map length is negative: %d".formatted(length));
        }
        if (length > input.remaining()) {
            throw new SchemaException("Map length %d is greater than the %d readable bytes"
                    .formatted(length, input.remaining())
            );
        }

        final Map<K, V> map = LinkedHashMap.newLinkedHashMap(length);
        for (int i = 0; i < length; i++) {
            final K key = this.keyType.read(input);
            final V entryValue = this.valueType.read(input);
            if (map.putIfAbsent(key, entryValue) != null) {
                throw new SchemaException("Duplicate map key at entry %d: %s".formatted(i, key));
            }
        }

        return map;
    }

    @Override
    public Class<Map<K, V>> javaType() {
        //noinspection unchecked
        return (Class<Map<K, V>>) (Class<?>) Map.class;
    }

    @Override
    public List<Part> parts(final Map<K, V> value) {
        final List<Part> parts = new ArrayList<>(value.size() * 2 + 1);

        parts.add(new Part("length", this.lengthType, value.size()));
        int index = 0;
        for (final Map.Entry<K, V> entry : value.entrySet()) {
            parts.add(new Part("[%d].key".formatted(index), this.keyType, entry.getKey()));
            parts.add(new Part("[%d].value".formatted(index), this.valueType, entry.getValue()));
            index++;
        }

        return parts;
    }

}
