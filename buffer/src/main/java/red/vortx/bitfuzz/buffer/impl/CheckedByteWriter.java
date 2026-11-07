// This file is part of BitFuzz.
// SPDX-FileCopyrightText: 2026 RedVortexDev
// SPDX-License-Identifier: Apache-2.0

package red.vortx.bitfuzz.buffer.impl;

import red.vortx.bitfuzz.buffer.ByteWriter;
import red.vortx.bitfuzz.buffer.WriteOverflowException;

/// A [ByteWriter] that checks for overflow before every write.
public abstract class CheckedByteWriter implements ByteWriter {

    private void require(final int bytes) {
        final int writable = this.writable();
        if (writable < bytes) {
            throw new WriteOverflowException("needed " + bytes + " bytes, " + writable + " writable");
        }
    }

    @Override
    public void writeByte(final byte value) {
        this.require(Byte.BYTES);
        this.writeByte0(value);
    }

    @Override
    public void writeShort(final short value) {
        this.require(Short.BYTES);
        this.writeShort0(value);
    }

    @Override
    public void writeShortLE(final short value) {
        this.require(Short.BYTES);
        this.writeShortLE0(value);
    }

    @Override
    public void writeInt(final int value) {
        this.require(Integer.BYTES);
        this.writeInt0(value);
    }

    @Override
    public void writeIntLE(final int value) {
        this.require(Integer.BYTES);
        this.writeIntLE0(value);
    }

    @Override
    public void writeLong(final long value) {
        this.require(Long.BYTES);
        this.writeLong0(value);
    }

    @Override
    public void writeLongLE(final long value) {
        this.require(Long.BYTES);
        this.writeLongLE0(value);
    }

    @Override
    public void writeFloat(final float value) {
        this.require(Float.BYTES);
        this.writeFloat0(value);
    }

    @Override
    public void writeFloatLE(final float value) {
        this.require(Float.BYTES);
        this.writeFloatLE0(value);
    }

    @Override
    public void writeDouble(final double value) {
        this.require(Double.BYTES);
        this.writeDouble0(value);
    }

    @Override
    public void writeDoubleLE(final double value) {
        this.require(Double.BYTES);
        this.writeDoubleLE0(value);
    }

    @Override
    public void writeBytes(final byte[] source) {
        this.require(source.length);
        this.writeBytes0(source);
    }

    protected abstract void writeByte0(byte value);

    protected abstract void writeShort0(short value);

    protected abstract void writeShortLE0(short value);

    protected abstract void writeInt0(int value);

    protected abstract void writeIntLE0(int value);

    protected abstract void writeLong0(long value);

    protected abstract void writeLongLE0(long value);

    protected abstract void writeFloat0(float value);

    protected abstract void writeFloatLE0(float value);

    protected abstract void writeDouble0(double value);

    protected abstract void writeDoubleLE0(double value);

    protected abstract void writeBytes0(byte[] source);

}
