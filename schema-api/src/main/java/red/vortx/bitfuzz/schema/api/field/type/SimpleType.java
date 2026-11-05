package red.vortx.bitfuzz.schema.api.field.type;

import red.vortx.bitfuzz.buffer.ByteReader;
import red.vortx.bitfuzz.buffer.ByteWriter;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

/// A type made from a write function and a read function.
///
/// @param javaType the Java type of the value for type checking
/// @param writer   the function that writes a value to the given output
/// @param reader   the function that reads a value from the given input
/// @param <T>      the type of the value
public record SimpleType<T>(Class<T> javaType, BiConsumer<ByteWriter, T> writer,
                            Function<ByteReader, T> reader) implements Type<T> {

    public SimpleType {
        Objects.requireNonNull(javaType, "javaType");
        Objects.requireNonNull(writer, "writer");
        Objects.requireNonNull(reader, "reader");
    }

    @Override
    public void write(final ByteWriter output, final T value) {
        this.writer.accept(output, value);
    }

    @Override
    public T read(final ByteReader input) {
        return this.reader.apply(input);
    }

}
