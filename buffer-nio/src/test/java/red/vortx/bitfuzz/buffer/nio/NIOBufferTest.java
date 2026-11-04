package red.vortx.bitfuzz.buffer.nio;

import org.junit.jupiter.api.Test;
import red.vortx.bitfuzz.buffer.BufferContractTest;
import red.vortx.bitfuzz.buffer.ByteReader;
import red.vortx.bitfuzz.buffer.ByteWriter;
import red.vortx.bitfuzz.buffer.WriteOverflowException;

import java.nio.ByteBuffer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NIOBufferTest extends BufferContractTest {

    private final ByteBuffer buffer = ByteBuffer.allocate(1024);

    @Override
    protected ByteWriter createWriter() {
        return new NIOByteWriter(this.buffer);
    }

    @Override
    protected ByteReader createReader() {
        return new NIOByteReader(this.buffer.flip());
    }

    @Test
    void writingPastCapacityThrowsOverflow() {
        final ByteBuffer small = ByteBuffer.allocate(3);
        final ByteWriter writer = new NIOByteWriter(small);

        assertThatThrownBy(() -> writer.writeInt(1)).isInstanceOf(WriteOverflowException.class);
        assertThatThrownBy(() -> writer.writeBytes(new byte[4])).isInstanceOf(WriteOverflowException.class);
        assertThat(small.position()).isZero();
    }

}
