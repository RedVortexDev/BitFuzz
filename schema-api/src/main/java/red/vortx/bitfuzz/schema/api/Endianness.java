// This file is part of BitFuzz.
// SPDX-FileCopyrightText: 2026 RedVortexDev
// SPDX-License-Identifier: Apache-2.0

package red.vortx.bitfuzz.schema.api;

/// A byte order.
public enum Endianness {
    BIG_ENDIAN, LITTLE_ENDIAN;

    /// {@return whether this is [#BIG_ENDIAN]}
    public boolean isBigEndian() {
        return this == BIG_ENDIAN;
    }

    /// {@return whether this is [#LITTLE_ENDIAN]}
    public boolean isLittleEndian() {
        return this == LITTLE_ENDIAN;
    }

    /// {@return the opposite endianness}
    public Endianness opposite() {
        return this == BIG_ENDIAN ? LITTLE_ENDIAN : BIG_ENDIAN;
    }

}
