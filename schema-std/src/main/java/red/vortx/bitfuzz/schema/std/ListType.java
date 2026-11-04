package red.vortx.bitfuzz.schema.std;

import red.vortx.bitfuzz.buffer.ByteReader;
import red.vortx.bitfuzz.buffer.ByteWriter;
import red.vortx.bitfuzz.schema.api.SchemaException;
import red.vortx.bitfuzz.schema.api.field.type.Type;

import java.util.ArrayList;
import java.util.List;

/// A list. An element count, then each element in order.
///
/// Reading rejects a negative count or one larger than the remaining bytes.
///
/// @param elementType the type of the elements
/// @param lengthType  the type of the count prefix
/// @param <T>         the type of the elements
public record ListType<T>(Type<T> elementType, Type<Integer> lengthType) implements Type<List<T>> {

    @Override
    public void write(final ByteWriter output, final List<T> value) {
        this.lengthType.write(output, value.size());
        for (final T element : value) {
            this.elementType.write(output, element);
        }
    }

    @Override
    public List<T> read(final ByteReader input) {
        final int length = this.lengthType.read(input);

        if (length < 0) {
            throw new SchemaException("List length is negative: %d".formatted(length));
        }
        if (length > input.remaining()) {
            throw new SchemaException("List length %d is greater than the %d readable bytes"
                    .formatted(length, input.remaining())
            );
        }

        final List<T> list = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            list.add(this.elementType.read(input));
        }

        return list;
    }

    @Override
    public Class<List<T>> javaType() {
        //noinspection unchecked
        return (Class<List<T>>) (Class<?>) List.class;
    }

}
