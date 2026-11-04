package red.vortx.bitfuzz.buffer.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;
import red.vortx.bitfuzz.buffer.BufferContractTest;
import red.vortx.bitfuzz.buffer.ByteReader;
import red.vortx.bitfuzz.buffer.ByteWriter;
import red.vortx.bitfuzz.buffer.WriteOverflowException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NettyBufferTest extends BufferContractTest {

    private final ByteBuf buffer = Unpooled.buffer();

    @Override
    protected ByteWriter createWriter() {
        return new NettyByteWriter(this.buffer);
    }

    @Override
    protected ByteReader createReader() {
        return new NettyByteReader(this.buffer);
    }

    @Test
    void writingPastMaxCapacityThrowsOverflow() {
        final ByteBuf small = Unpooled.buffer(0, 3);
        final ByteWriter writer = new NettyByteWriter(small);

        assertThatThrownBy(() -> writer.writeInt(1)).isInstanceOf(WriteOverflowException.class);
        assertThatThrownBy(() -> writer.writeBytes(new byte[4])).isInstanceOf(WriteOverflowException.class);
        assertThat(small.writerIndex()).isZero();
    }

}
