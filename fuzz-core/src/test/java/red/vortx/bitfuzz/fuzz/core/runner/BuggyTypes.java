// This file is part of BitFuzz.
// SPDX-FileCopyrightText: 2026 RedVortexDev
// SPDX-License-Identifier: Apache-2.0

package red.vortx.bitfuzz.fuzz.core.runner;

import red.vortx.bitfuzz.schema.api.field.type.SimpleType;
import red.vortx.bitfuzz.schema.api.field.type.Type;

import java.nio.charset.StandardCharsets;

/// Deliberately broken types for checking that the runners catch real bugs.
enum BuggyTypes {
    ;

    /// Writes only the low byte of an int, so most values come back different.
    static final Type<Integer> LOSSY_INT = new SimpleType<>(Integer.class,
            (out, value) -> out.writeByte((byte) (int) value),
            in -> (int) in.readByte()
    );

    /// Writes a padding byte it never reads, so a byte is always left over.
    static final Type<Boolean> PADDED_BOOL = new SimpleType<>(Boolean.class,
            (out, value) -> {
                out.writeByte(value ? (byte) 1 : (byte) 0);
                out.writeByte((byte) 0);
            },
            in -> in.readByte() == 1
    );

    /// A string that forgets to check its length: a negative length crashes with
    /// [NegativeArraySizeException] instead of being rejected.
    static final Type<String> UNCHECKED_STRING = new SimpleType<>(String.class,
            (out, value) -> {
                final byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
                out.writeInt(bytes.length);
                out.writeBytes(bytes);
            },
            in -> {
                final byte[] bytes = new byte[in.readInt()];
                in.readBytes(bytes);
                return new String(bytes, StandardCharsets.UTF_8);
            }
    );

}
