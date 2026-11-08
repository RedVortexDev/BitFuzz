// This file is part of BitFuzz.
// SPDX-FileCopyrightText: 2026 RedVortexDev
// SPDX-License-Identifier: Apache-2.0

package red.vortx.bitfuzz.schema.std.type;

import red.vortx.bitfuzz.buffer.ByteReader;
import red.vortx.bitfuzz.buffer.ByteWriter;
import red.vortx.bitfuzz.schema.api.SchemaException;

/// A boolean. One byte, `0` for false and `1` for true. Any other value is rejected.
public record BooleanType() implements StandardType<Boolean> {

    public static final byte TRUE = (byte) 1;
    public static final byte FALSE = (byte) 0;

    @Override
    public void write(final ByteWriter output, final Boolean value) {
        output.writeByte(value ? TRUE : FALSE);
    }

    @Override
    public Boolean read(final ByteReader input) {
        final byte value = input.readByte();
        return switch (value) {
            case TRUE -> true;
            case FALSE -> false;
            default -> throw new SchemaException("Invalid boolean value: %d".formatted(value));
        };
    }

    @Override
    public Class<Boolean> javaType() {
        return Boolean.class;
    }

}
