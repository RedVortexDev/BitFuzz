package red.vortx.bitfuzz.fuzz.core;

import red.vortx.bitfuzz.buffer.impl.CountingByteWriter;
import red.vortx.bitfuzz.buffer.nio.NIOByteReader;
import red.vortx.bitfuzz.buffer.nio.NIOByteWriter;
import red.vortx.bitfuzz.schema.api.Schema;

import java.nio.ByteBuffer;
import java.util.List;

/// Converts between schema values and plain byte arrays.
public enum Bytes {
    ;

    /// {@return `values` encoded with `schema`}
    public static byte[] encode(final Schema schema, final List<Object> values) {
        final CountingByteWriter counter = new CountingByteWriter();
        schema.write(counter, values);

        final byte[] bytes = new byte[counter.count()];
        schema.write(new NIOByteWriter(ByteBuffer.wrap(bytes)), values);
        return bytes;
    }

    /// {@return the values decoded from `bytes`, ignoring any bytes left over}
    public static List<Object> decode(final Schema schema, final byte[] bytes) {
        return schema.read(new NIOByteReader(ByteBuffer.wrap(bytes)));
    }

}
