package red.vortx.bitfuzz.schema.api.field.type;

import red.vortx.bitfuzz.buffer.ByteReader;
import red.vortx.bitfuzz.buffer.ByteWriter;
import red.vortx.bitfuzz.buffer.impl.CountingByteWriter;
import red.vortx.bitfuzz.schema.api.SchemaDefaults;

/// Writes and reads values of one Java type.
///
/// @param <T> the type of the value
public interface Type<T> extends TypeSpec<T> {

    /// Writes a value to the given output.
    ///
    /// @param output the output to write to
    /// @param value  the value to write
    void write(ByteWriter output, T value);

    /// Writes a value whose type isn't known at compile time.
    ///
    /// @param output the output to write to
    /// @param value  the value to write
    /// @throws ClassCastException if `value` isn't a `T`
    default void writeUnchecked(final ByteWriter output, final Object value) {
        //noinspection unchecked
        final T typedValue = (T) value;
        this.write(output, typedValue);
    }

    /// Reads a value from the given input.
    ///
    /// @param input the input to read from
    /// @return the read value
    T read(ByteReader input);

    /// {@return the class of the values}
    Class<T> javaType();

    /// {@return how many bytes `value` takes when written}
    ///
    /// @param value the value to measure
    default int sizeOf(final T value) {
        final CountingByteWriter counter = new CountingByteWriter();
        this.write(counter, value);
        return counter.count();
    }

    @Override
    default Type<T> resolve(final SchemaDefaults defaults) {
        return this;
    }

}
