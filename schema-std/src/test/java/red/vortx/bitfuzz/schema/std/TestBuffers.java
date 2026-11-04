package red.vortx.bitfuzz.schema.std;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import red.vortx.bitfuzz.buffer.ByteReader;
import red.vortx.bitfuzz.buffer.ByteWriter;
import red.vortx.bitfuzz.buffer.netty.NettyByteReader;
import red.vortx.bitfuzz.buffer.netty.NettyByteWriter;

import java.util.function.Consumer;

enum TestBuffers {
    ;

    static byte[] written(final Consumer<ByteWriter> write) {
        final ByteBuf buffer = Unpooled.buffer();
        write.accept(new NettyByteWriter(buffer));
        return ByteBufUtil.getBytes(buffer);
    }

    static ByteReader reader(final byte[] bytes) {
        return new NettyByteReader(Unpooled.wrappedBuffer(bytes));
    }

    static ByteReader reader(final int... bytes) {
        final byte[] array = new byte[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            array[i] = (byte) bytes[i];
        }
        return reader(array);
    }

}
