// This file is part of BitFuzz.
// SPDX-FileCopyrightText: 2026 RedVortexDev
// SPDX-License-Identifier: Apache-2.0

package red.vortx.bitfuzz.buffer.impl;

import red.vortx.bitfuzz.buffer.ByteReader;
import red.vortx.bitfuzz.buffer.ReadUnderflowException;

/// A [ByteReader] that checks for underflow before every read.
public abstract class CheckedByteReader implements ByteReader {

    private void require(final int bytes) {
        if (this.remaining() < bytes) {
            throw new ReadUnderflowException("needed " + bytes + " bytes, " + this.remaining() + " remaining");
        }
    }

    @Override
    public byte readByte() {
        this.require(Byte.BYTES);
        return this.readByte0();
    }

    @Override
    public short readShort() {
        this.require(Short.BYTES);
        return this.readShort0();
    }

    @Override
    public short readShortLE() {
        this.require(Short.BYTES);
        return this.readShortLE0();
    }

    @Override
    public int readInt() {
        this.require(Integer.BYTES);
        return this.readInt0();
    }

    @Override
    public int readIntLE() {
        this.require(Integer.BYTES);
        return this.readIntLE0();
    }

    @Override
    public long readLong() {
        this.require(Long.BYTES);
        return this.readLong0();
    }

    @Override
    public long readLongLE() {
        this.require(Long.BYTES);
        return this.readLongLE0();
    }

    @Override
    public float readFloat() {
        this.require(Float.BYTES);
        return this.readFloat0();
    }

    @Override
    public float readFloatLE() {
        this.require(Float.BYTES);
        return this.readFloatLE0();
    }

    @Override
    public double readDouble() {
        this.require(Double.BYTES);
        return this.readDouble0();
    }

    @Override
    public double readDoubleLE() {
        this.require(Double.BYTES);
        return this.readDoubleLE0();
    }

    @Override
    public void readBytes(final byte[] destination) {
        this.require(destination.length);
        this.readBytes0(destination);
    }

    protected abstract byte readByte0();

    protected abstract short readShort0();

    protected abstract short readShortLE0();

    protected abstract int readInt0();

    protected abstract int readIntLE0();

    protected abstract long readLong0();

    protected abstract long readLongLE0();

    protected abstract float readFloat0();

    protected abstract float readFloatLE0();

    protected abstract double readDouble0();

    protected abstract double readDoubleLE0();

    protected abstract void readBytes0(byte[] destination);

}
