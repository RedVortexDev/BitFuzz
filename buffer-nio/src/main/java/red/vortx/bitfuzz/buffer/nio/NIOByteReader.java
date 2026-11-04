package red.vortx.bitfuzz.buffer.nio;

import red.vortx.bitfuzz.buffer.impl.CheckedByteReader;

import java.nio.ByteBuffer;

import static red.vortx.bitfuzz.buffer.nio.NIOBufferUtility.bigEndian;
import static red.vortx.bitfuzz.buffer.nio.NIOBufferUtility.littleEndian;

/// A reader for primitives backed by a [NIO ByteBuffer][ByteBuffer].
///
/// Reads use the byte order of the method called, not the buffer's [order][ByteBuffer#order()].
public final class NIOByteReader extends CheckedByteReader {

    private final ByteBuffer buffer;

    /// Reads from `buffer`, starting at its position.
    ///
    /// @param buffer the underlying [NIO ByteBuffer][ByteBuffer]
    public NIOByteReader(final ByteBuffer buffer) {
        this.buffer = buffer;
    }

    @Override
    protected byte readByte0() {
        return this.buffer.get();
    }

    @Override
    protected short readShort0() {
        return bigEndian(this.buffer, this.buffer.getShort());
    }

    @Override
    protected short readShortLE0() {
        return littleEndian(this.buffer, this.buffer.getShort());
    }

    @Override
    protected int readInt0() {
        return bigEndian(this.buffer, this.buffer.getInt());
    }

    @Override
    protected int readIntLE0() {
        return littleEndian(this.buffer, this.buffer.getInt());
    }

    @Override
    protected long readLong0() {
        return bigEndian(this.buffer, this.buffer.getLong());
    }

    @Override
    protected long readLongLE0() {
        return littleEndian(this.buffer, this.buffer.getLong());
    }

    @Override
    protected float readFloat0() {
        return Float.intBitsToFloat(this.readInt0());
    }

    @Override
    protected float readFloatLE0() {
        return Float.intBitsToFloat(this.readIntLE0());
    }

    @Override
    protected double readDouble0() {
        return Double.longBitsToDouble(this.readLong0());
    }

    @Override
    protected double readDoubleLE0() {
        return Double.longBitsToDouble(this.readLongLE0());
    }

    @Override
    protected void readBytes0(final byte[] destination) {
        this.buffer.get(destination);
    }

    @Override
    public int remaining() {
        return this.buffer.remaining();
    }

    /// {@return the underlying [NIO ByteBuffer][ByteBuffer]}
    public ByteBuffer buffer() {
        return this.buffer;
    }

}
