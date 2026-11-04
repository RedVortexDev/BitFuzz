package red.vortx.bitfuzz.buffer;

import red.vortx.bitfuzz.buffer.impl.CheckedByteReader;

/// A reader for primitives.
///
/// Implementations must throw [ReadUnderflowException] when reading past the remaining bytes.
/// Extending [CheckedByteReader] adds this check to every method.
public interface ByteReader {

    /// Reads a byte.
    ///
    /// @return the read byte
    byte readByte();

    /// Reads a big-endian short.
    ///
    /// @return the read short
    short readShort();

    /// Reads a little-endian short.
    ///
    /// @return the read short
    short readShortLE();

    /// Reads a big-endian integer.
    ///
    /// @return the read integer
    int readInt();

    /// Reads a little-endian integer.
    ///
    /// @return the read integer
    int readIntLE();

    /// Reads a big-endian long.
    ///
    /// @return the read long
    long readLong();

    /// Reads a little-endian long.
    ///
    /// @return the read long
    long readLongLE();

    /// Reads a big-endian float.
    ///
    /// @return the read float
    float readFloat();

    /// Reads a little-endian float.
    ///
    /// @return the read float
    float readFloatLE();

    /// Reads a big-endian double.
    ///
    /// @return the read double
    double readDouble();

    /// Reads a little-endian double.
    ///
    /// @return the read double
    double readDoubleLE();

    /// Reads `destination.length` bytes into `destination`.
    ///
    /// @param destination the array to read bytes into
    void readBytes(byte[] destination);

    /// {@return the number of remaining bytes}
    int remaining();

}
