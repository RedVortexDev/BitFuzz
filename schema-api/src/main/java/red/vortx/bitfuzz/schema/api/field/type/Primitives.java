// This file is part of BitFuzz.
// SPDX-FileCopyrightText: 2026 RedVortexDev
// SPDX-License-Identifier: Apache-2.0

package red.vortx.bitfuzz.schema.api.field.type;

import red.vortx.bitfuzz.buffer.ByteReader;
import red.vortx.bitfuzz.buffer.ByteWriter;

/// Primitive types with a fixed byte order.
///
/// For types that follow the schema's endianness, use `Types` from schema-std.
public enum Primitives {
    ;

    /// A byte.
    public static final Type<Byte> BYTE = new SimpleType<>(Byte.class, ByteWriter::writeByte, ByteReader::readByte);
    /// A big-endian short.
    public static final Type<Short> SHORT = new SimpleType<>(Short.class, ByteWriter::writeShort, ByteReader::readShort);
    /// A little-endian short.
    public static final Type<Short> SHORT_LE = new SimpleType<>(Short.class, ByteWriter::writeShortLE, ByteReader::readShortLE);
    /// A big-endian integer.
    public static final Type<Integer> INT = new SimpleType<>(Integer.class, ByteWriter::writeInt, ByteReader::readInt);
    /// A little-endian integer.
    public static final Type<Integer> INT_LE = new SimpleType<>(Integer.class, ByteWriter::writeIntLE, ByteReader::readIntLE);
    /// A big-endian long.
    public static final Type<Long> LONG = new SimpleType<>(Long.class, ByteWriter::writeLong, ByteReader::readLong);
    /// A little-endian long.
    public static final Type<Long> LONG_LE = new SimpleType<>(Long.class, ByteWriter::writeLongLE, ByteReader::readLongLE);
    /// A big-endian float.
    public static final Type<Float> FLOAT = new SimpleType<>(Float.class, ByteWriter::writeFloat, ByteReader::readFloat);
    /// A little-endian float.
    public static final Type<Float> FLOAT_LE = new SimpleType<>(Float.class, ByteWriter::writeFloatLE, ByteReader::readFloatLE);
    /// A big-endian double.
    public static final Type<Double> DOUBLE = new SimpleType<>(Double.class, ByteWriter::writeDouble, ByteReader::readDouble);
    /// A little-endian double.
    public static final Type<Double> DOUBLE_LE = new SimpleType<>(Double.class, ByteWriter::writeDoubleLE, ByteReader::readDoubleLE);

}
