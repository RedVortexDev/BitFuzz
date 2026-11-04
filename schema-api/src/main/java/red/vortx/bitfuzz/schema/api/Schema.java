package red.vortx.bitfuzz.schema.api;

import red.vortx.bitfuzz.buffer.ByteReader;
import red.vortx.bitfuzz.buffer.ByteWriter;
import red.vortx.bitfuzz.schema.api.field.Field;

import java.util.List;

/// An ordered list of fields. Writes and reads one value per field.
public interface Schema {

    /// Writes the given values to the given output.
    ///
    /// Values must exactly match the fields of this schema in order and type.
    ///
    /// @param output the output to write to
    /// @param values the values to write
    /// @throws SchemaException for any error that occurs while writing the values
    void write(ByteWriter output, List<Object> values) throws SchemaException;

    /// Reads one value per field from the given input. Bytes after the last field are left unread.
    ///
    /// Values will exactly match the fields of this schema in order and type.
    ///
    /// @param input the input to read from
    /// @return the read values
    /// @throws SchemaException for any error that occurs while reading the values
    List<Object> read(ByteReader input) throws SchemaException;

    /// {@return the fields of this schema}
    List<Field> fields();

}
