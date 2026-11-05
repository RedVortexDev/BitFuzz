package red.vortx.bitfuzz.buffer.nio;

import red.vortx.bitfuzz.buffer.WriteOverflowException;
import red.vortx.bitfuzz.buffer.impl.CheckedByteWriter;

import java.nio.ByteBuffer;
import java.util.Objects;

import static red.vortx.bitfuzz.buffer.nio.NIOBufferUtility.bigEndian;
import static red.vortx.bitfuzz.buffer.nio.NIOBufferUtility.littleEndian;

/// A writer for primitives backed by a [NIO ByteBuffer][ByteBuffer].
///
/// Writes use the byte order of the method called, not the buffer's [order][ByteBuffer#order()].
public final class NIOByteWriter extends CheckedByteWriter {

    private final ByteBuffer buffer;

    /// Writes to `buffer`, starting at its position.
    ///
    /// @param buffer the underlying [NIO ByteBuffer][ByteBuffer]
    public NIOByteWriter(final ByteBuffer buffer) {
        this.buffer = Objects.requireNonNull(buffer, "buffer");
    }

    @Override
    public int writable() {
        return this.buffer.remaining();
    }

    @Override
    protected void writeByte0(final byte value) {
        this.buffer.put(value);
    }

    @Override
    protected void writeShort0(final short value) {
        this.buffer.putShort(bigEndian(this.buffer, value));
    }

    @Override
    protected void writeShortLE0(final short value) {
        this.buffer.putShort(littleEndian(this.buffer, value));
    }

    @Override
    protected void writeInt0(final int value) {
        this.buffer.putInt(bigEndian(this.buffer, value));
    }

    @Override
    protected void writeIntLE0(final int value) {
        this.buffer.putInt(littleEndian(this.buffer, value));
    }

    @Override
    protected void writeLong0(final long value) {
        this.buffer.putLong(bigEndian(this.buffer, value));
    }

    @Override
    protected void writeLongLE0(final long value) {
        this.buffer.putLong(littleEndian(this.buffer, value));
    }

    @Override
    protected void writeFloat0(final float value) {
        this.writeInt0(Float.floatToRawIntBits(value));
    }

    @Override
    protected void writeFloatLE0(final float value) {
        this.writeIntLE0(Float.floatToRawIntBits(value));
    }

    @Override
    protected void writeDouble0(final double value) {
        this.writeLong0(Double.doubleToRawLongBits(value));
    }

    @Override
    protected void writeDoubleLE0(final double value) {
        this.writeLongLE0(Double.doubleToRawLongBits(value));
    }

    @Override
    protected void writeBytes0(final byte[] source) {
        this.buffer.put(source);
    }

    /// {@return the underlying [NIO ByteBuffer][ByteBuffer]}
    public ByteBuffer buffer() {
        return this.buffer;
    }

}
