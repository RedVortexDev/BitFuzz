// This file is part of BitFuzz.
// SPDX-FileCopyrightText: 2026 RedVortexDev
// SPDX-License-Identifier: Apache-2.0

package red.vortx.bitfuzz.buffer.netty;

import io.netty.buffer.ByteBuf;
import red.vortx.bitfuzz.buffer.impl.CheckedByteReader;

import java.util.Objects;

/// A reader for primitives backed by a [Netty ByteBuf][ByteBuf].
public final class NettyByteReader extends CheckedByteReader {

    private final ByteBuf buffer;

    /// Reads from `buffer`, starting at its reader index.
    ///
    /// @param buffer the underlying [Netty ByteBuf][ByteBuf]
    public NettyByteReader(final ByteBuf buffer) {
        this.buffer = Objects.requireNonNull(buffer, "buffer");
    }

    @Override
    protected byte readByte0() {
        return this.buffer.readByte();
    }

    @Override
    protected short readShort0() {
        return this.buffer.readShort();
    }

    @Override
    protected short readShortLE0() {
        return this.buffer.readShortLE();
    }

    @Override
    protected int readInt0() {
        return this.buffer.readInt();
    }

    @Override
    protected int readIntLE0() {
        return this.buffer.readIntLE();
    }

    @Override
    protected long readLong0() {
        return this.buffer.readLong();
    }

    @Override
    protected long readLongLE0() {
        return this.buffer.readLongLE();
    }

    @Override
    protected float readFloat0() {
        return this.buffer.readFloat();
    }

    @Override
    protected float readFloatLE0() {
        return this.buffer.readFloatLE();
    }

    @Override
    protected double readDouble0() {
        return this.buffer.readDouble();
    }

    @Override
    protected double readDoubleLE0() {
        return this.buffer.readDoubleLE();
    }

    @Override
    protected void readBytes0(final byte[] destination) {
        this.buffer.readBytes(destination);
    }

    @Override
    public int remaining() {
        return this.buffer.readableBytes();
    }

    /// {@return the underlying [Netty ByteBuf][ByteBuf]}
    public ByteBuf buffer() {
        return this.buffer;
    }

}
