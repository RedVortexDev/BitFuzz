package red.vortx.bitfuzz.schema.core;

import red.vortx.bitfuzz.buffer.ByteReader;
import red.vortx.bitfuzz.buffer.ByteWriter;
import red.vortx.bitfuzz.schema.api.field.type.SimpleType;
import red.vortx.bitfuzz.schema.api.field.type.Type;

/// Primitive types with a fixed byte order.
///
/// For types that follow the schema's endianness, use `Types` from schema-std.
public enum Primitives {
    ;

    public static final Type<Byte> BYTE = new SimpleType<>(Byte.class, ByteWriter::writeByte, ByteReader::readByte);
    public static final Type<Short> SHORT = new SimpleType<>(Short.class, ByteWriter::writeShort, ByteReader::readShort);
    public static final Type<Short> SHORT_LE = new SimpleType<>(Short.class, ByteWriter::writeShortLE, ByteReader::readShortLE);
    public static final Type<Integer> INT = new SimpleType<>(Integer.class, ByteWriter::writeInt, ByteReader::readInt);
    public static final Type<Integer> INT_LE = new SimpleType<>(Integer.class, ByteWriter::writeIntLE, ByteReader::readIntLE);
    public static final Type<Long> LONG = new SimpleType<>(Long.class, ByteWriter::writeLong, ByteReader::readLong);
    public static final Type<Long> LONG_LE = new SimpleType<>(Long.class, ByteWriter::writeLongLE, ByteReader::readLongLE);
    public static final Type<Float> FLOAT = new SimpleType<>(Float.class, ByteWriter::writeFloat, ByteReader::readFloat);
    public static final Type<Float> FLOAT_LE = new SimpleType<>(Float.class, ByteWriter::writeFloatLE, ByteReader::readFloatLE);
    public static final Type<Double> DOUBLE = new SimpleType<>(Double.class, ByteWriter::writeDouble, ByteReader::readDouble);
    public static final Type<Double> DOUBLE_LE = new SimpleType<>(Double.class, ByteWriter::writeDoubleLE, ByteReader::readDoubleLE);

}
