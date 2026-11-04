package red.vortx.bitfuzz.buffer.nio;

import org.junit.jupiter.api.AfterEach;
import red.vortx.bitfuzz.buffer.BufferContractTest;
import red.vortx.bitfuzz.buffer.ByteReader;
import red.vortx.bitfuzz.buffer.ByteWriter;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.assertj.core.api.Assertions.assertThat;

class NIOLittleEndianBufferTest extends BufferContractTest {

    private final ByteBuffer buffer = ByteBuffer.allocate(1024).order(ByteOrder.LITTLE_ENDIAN);

    @Override
    protected ByteWriter createWriter() {
        return new NIOByteWriter(this.buffer);
    }

    @Override
    protected ByteReader createReader() {
        return new NIOByteReader(this.buffer.flip());
    }

    @AfterEach
    void orderIsUnchanged() {
        assertThat(this.buffer.order()).isEqualTo(ByteOrder.LITTLE_ENDIAN);
    }

}
