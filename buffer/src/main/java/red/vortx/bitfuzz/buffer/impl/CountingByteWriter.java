package red.vortx.bitfuzz.buffer.impl;

import red.vortx.bitfuzz.buffer.ByteWriter;

/// A [ByteWriter] that only counts the number of bytes written.
///
/// @see #count()
public class CountingByteWriter implements ByteWriter {

    private int count = 0;

    public CountingByteWriter() {
    }

    /// Starts counting at `initialCount`.
    ///
    /// @param initialCount the count to start from
    public CountingByteWriter(final int initialCount) {
        this.count = initialCount;
    }

    @Override
    public void writeByte(final byte value) {
        this.count += Byte.BYTES;
    }

    @Override
    public void writeShort(final short value) {
        this.count += Short.BYTES;
    }

    @Override
    public void writeShortLE(final short value) {
        this.writeShort(value);
    }

    @Override
    public void writeInt(final int value) {
        this.count += Integer.BYTES;
    }

    @Override
    public void writeIntLE(final int value) {
        this.writeInt(value);
    }

    @Override
    public void writeLong(final long value) {
        this.count += Long.BYTES;
    }

    @Override
    public void writeLongLE(final long value) {
        this.writeLong(value);
    }

    @Override
    public void writeFloat(final float value) {
        this.count += Float.BYTES;
    }

    @Override
    public void writeFloatLE(final float value) {
        this.writeFloat(value);
    }

    @Override
    public void writeDouble(final double value) {
        this.count += Double.BYTES;
    }

    @Override
    public void writeDoubleLE(final double value) {
        this.writeDouble(value);
    }

    @Override
    public void writeBytes(final byte[] source) {
        this.count += source.length;
    }

    @Override
    public int writable() {
        return Integer.MAX_VALUE;
    }

    /// {@return the number of bytes written so far}
    public int count() {
        return this.count;
    }

}
