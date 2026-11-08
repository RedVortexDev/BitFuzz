// This file is part of BitFuzz.
// SPDX-FileCopyrightText: 2026 RedVortexDev
// SPDX-License-Identifier: Apache-2.0

package red.vortx.bitfuzz.schema.std.type;

import red.vortx.bitfuzz.buffer.ByteReader;
import red.vortx.bitfuzz.buffer.ByteWriter;
import red.vortx.bitfuzz.schema.api.SchemaException;

/// A fixed-length UTF-8 string, with no length prefix.
///
/// Not padded, writing a string of any other byte length throws [SchemaException].
///
/// @param byteLength the number of bytes
public record FixedStringType(int byteLength) implements StandardType<String> {

    public FixedStringType {
        if (byteLength < 0) {
            throw new IllegalArgumentException("Byte length is negative: %d".formatted(byteLength));
        }
    }

    @Override
    public void write(final ByteWriter output, final String value) {
        final byte[] bytes = Utf8.encode(value);

        if (bytes.length != this.byteLength) {
            throw new SchemaException("Fixed string must be %d bytes but is %d"
                    .formatted(this.byteLength, bytes.length)
            );
        }

        output.writeBytes(bytes);
    }

    @Override
    public String read(final ByteReader input) {
        if (this.byteLength > input.remaining()) {
            throw new SchemaException("Fixed string of %d bytes is longer than the %d readable bytes"
                    .formatted(this.byteLength, input.remaining())
            );
        }

        final byte[] bytes = new byte[this.byteLength];
        input.readBytes(bytes);
        return Utf8.decode(bytes);
    }

    @Override
    public Class<String> javaType() {
        return String.class;
    }

}
