package red.vortx.bitfuzz.buffer.netty;

import io.netty.buffer.ByteBuf;
import red.vortx.bitfuzz.buffer.impl.CheckedByteWriter;

import java.util.Objects;

/// A writer for primitives backed by a [Netty ByteBuf][ByteBuf].
public final class NettyByteWriter extends CheckedByteWriter {

    private final ByteBuf buffer;

    /// Writes to `buffer`, starting at its writer index.
    ///
    /// @param buffer the underlying [Netty ByteBuf][ByteBuf]
    public NettyByteWriter(final ByteBuf buffer) {
        this.buffer = Objects.requireNonNull(buffer, "buffer");
    }

    @Override
    public int writable() {
        return this.buffer.maxWritableBytes();
    }

    @Override
    protected void writeByte0(final byte value) {
        this.buffer.writeByte(value);
    }

    @Override
    protected void writeShort0(final short value) {
        this.buffer.writeShort(value);
    }

    @Override
    protected void writeShortLE0(final short value) {
        this.buffer.writeShortLE(value);
    }

    @Override
    protected void writeInt0(final int value) {
        this.buffer.writeInt(value);
    }

    @Override
    protected void writeIntLE0(final int value) {
        this.buffer.writeIntLE(value);
    }

    @Override
    protected void writeLong0(final long value) {
        this.buffer.writeLong(value);
    }

    @Override
    protected void writeLongLE0(final long value) {
        this.buffer.writeLongLE(value);
    }

    @Override
    protected void writeFloat0(final float value) {
        this.buffer.writeFloat(value);
    }

    @Override
    protected void writeFloatLE0(final float value) {
        this.buffer.writeFloatLE(value);
    }

    @Override
    protected void writeDouble0(final double value) {
        this.buffer.writeDouble(value);
    }

    @Override
    protected void writeDoubleLE0(final double value) {
        this.buffer.writeDoubleLE(value);
    }

    @Override
    protected void writeBytes0(final byte[] source) {
        this.buffer.writeBytes(source);
    }

    /// {@return the underlying [Netty ByteBuf][ByteBuf]}
    public ByteBuf buffer() {
        return this.buffer;
    }

}
