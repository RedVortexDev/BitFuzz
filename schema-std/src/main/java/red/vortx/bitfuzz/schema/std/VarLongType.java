package red.vortx.bitfuzz.schema.std;

import red.vortx.bitfuzz.buffer.ByteReader;
import red.vortx.bitfuzz.buffer.ByteWriter;
import red.vortx.bitfuzz.schema.api.SchemaException;
import red.vortx.bitfuzz.schema.api.field.type.Type;

/// A variable-length 64-bit integer. Encoded like [VarIntType], but up to 10 bytes. Negative values always take
/// 10 bytes.
///
/// Reading rejects encodings longer than 10 bytes and a 10th byte that carries bits beyond 64. Overlong but otherwise
/// valid encodings are accepted.
public record VarLongType() implements Type<Long> {

    static final int MAX_BYTES = 10;

    @Override
    public void write(final ByteWriter output, final Long value) {
        long remaining = value;
        while ((remaining & ~0x7FL) != 0) {
            output.writeByte((byte) ((remaining & 0x7F) | 0x80));
            remaining >>>= 7;
        }
        output.writeByte((byte) remaining);
    }

    @Override
    public Long read(final ByteReader input) {
        long result = 0;
        for (int i = 0; i < MAX_BYTES; i++) {
            final byte current = input.readByte();
            if (i == MAX_BYTES - 1 && (current & 0xFE) != 0) {
                throw new SchemaException("VarLong is longer than %d bytes or overflows 64 bits".formatted(MAX_BYTES));
            }
            result |= (long) (current & 0x7F) << (7 * i);
            if ((current & 0x80) == 0) {
                return result;
            }
        }
        throw new SchemaException("VarLong is longer than %d bytes".formatted(MAX_BYTES));
    }

    @Override
    public Class<Long> javaType() {
        return Long.class;
    }

    @Override
    public int sizeOf(final Long value) {
        final int bits = Long.SIZE - Long.numberOfLeadingZeros(value);
        return Math.max(1, (bits + 6) / 7);
    }

}
