package red.vortx.bitfuzz.buffer;

import red.vortx.bitfuzz.buffer.impl.CheckedByteWriter;

/// A writer for primitives.
///
/// Implementations must throw [WriteOverflowException] when writing past the writable bytes.
/// Extending [CheckedByteWriter] adds this check to every method.
public interface ByteWriter {

    /// Writes a byte.
    ///
    /// @param value byte to write
    void writeByte(byte value);

    /// Writes a big-endian short.
    ///
    /// @param value short to write
    void writeShort(short value);

    /// Writes a little-endian short.
    ///
    /// @param value short to write
    void writeShortLE(short value);

    /// Writes a big-endian integer.
    ///
    /// @param value integer to write
    void writeInt(int value);

    /// Writes a little-endian integer.
    ///
    /// @param value integer to write
    void writeIntLE(int value);

    /// Writes a big-endian long.
    ///
    /// @param value long to write
    void writeLong(long value);

    /// Writes a little-endian long.
    ///
    /// @param value long to write
    void writeLongLE(long value);

    /// Writes a big-endian float.
    ///
    /// @param value float to write
    void writeFloat(float value);

    /// Writes a little-endian float.
    ///
    /// @param value float to write
    void writeFloatLE(float value);

    /// Writes a big-endian double.
    ///
    /// @param value double to write
    void writeDouble(double value);

    /// Writes a little-endian double.
    ///
    /// @param value double to write
    void writeDoubleLE(double value);

    /// Writes all of `source`.
    ///
    /// @param source the array to write bytes from
    void writeBytes(byte[] source);

    /// {@return how many more bytes can be written, counting any growth the buffer allows, or [Integer#MAX_VALUE]
    /// if unbounded or unknown}
    int writable();

}
