// This file is part of BitFuzz.
// SPDX-FileCopyrightText: 2026 RedVortexDev
// SPDX-License-Identifier: Apache-2.0

package red.vortx.bitfuzz.schema.std.type;

import red.vortx.bitfuzz.buffer.ByteReader;
import red.vortx.bitfuzz.buffer.ByteWriter;
import red.vortx.bitfuzz.schema.api.SchemaException;

/// A variable-length 32-bit integer. 7 bits per byte, least significant group first, with the high bit set on every
/// byte except the last. Negative values always take 5 bytes.
///
/// Reading rejects encodings longer than 5 bytes and a 5th byte that carries bits beyond 32. Overlong but otherwise
/// valid encodings (e.g. `80 00` for 0) are accepted.
public record VarIntType() implements StandardType<Integer> {

    static final int MAX_BYTES = 5;

    @Override
    public void write(final ByteWriter output, final Integer value) {
        int remaining = value;
        while ((remaining & ~0x7F) != 0) {
            output.writeByte((byte) ((remaining & 0x7F) | 0x80));
            remaining >>>= 7;
        }
        output.writeByte((byte) remaining);
    }

    @Override
    public Integer read(final ByteReader input) {
        int result = 0;
        for (int i = 0; i < MAX_BYTES; i++) {
            final byte current = input.readByte();
            if (i == MAX_BYTES - 1 && (current & 0xF0) != 0) {
                throw new SchemaException("VarInt is longer than %d bytes or overflows 32 bits".formatted(MAX_BYTES));
            }
            result |= (current & 0x7F) << (7 * i);
            if ((current & 0x80) == 0) {
                return result;
            }
        }
        throw new SchemaException("VarInt is longer than %d bytes".formatted(MAX_BYTES));
    }

    @Override
    public Class<Integer> javaType() {
        return Integer.class;
    }

    @Override
    public int sizeOf(final Integer value) {
        final int bits = Integer.SIZE - Integer.numberOfLeadingZeros(value);
        return Math.max(1, (bits + 6) / 7);
    }

}
