// This file is part of BitFuzz.
// SPDX-FileCopyrightText: 2026 RedVortexDev
// SPDX-License-Identifier: Apache-2.0

package red.vortx.bitfuzz.buffer.nio;

import red.vortx.bitfuzz.buffer.BufferContractTest;
import red.vortx.bitfuzz.buffer.ByteReader;
import red.vortx.bitfuzz.buffer.ByteWriter;

import java.nio.ByteBuffer;

class NIODirectBufferTest extends BufferContractTest {

    private final ByteBuffer buffer = ByteBuffer.allocateDirect(1024);

    @Override
    protected ByteWriter createWriter() {
        return new NIOByteWriter(this.buffer);
    }

    @Override
    protected ByteReader createReader() {
        return new NIOByteReader(this.buffer.flip());
    }

}
