package red.vortx.bitfuzz.buffer;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/// Contract every [ByteReader] and [ByteWriter] implementation must satisfy.
///
/// Each test writes everything before reading, since some buffers share one position for both.
public abstract class BufferContractTest {

    private @Nullable ByteWriter writer;
    private @Nullable ByteReader reader;

    protected abstract ByteWriter createWriter();

    protected abstract ByteReader createReader();

    protected final ByteWriter writer() {
        if (this.reader != null) {
            throw new IllegalStateException("test wrote after it started reading");
        }
        if (this.writer == null) {
            this.writer = this.createWriter();
        }
        return this.writer;
    }

    protected final ByteReader reader() {
        if (this.reader == null) {
            this.writer();
            this.reader = this.createReader();
        }
        return this.reader;
    }

    @ParameterizedTest
    @ValueSource(bytes = {0, 1, -1, 0x12, Byte.MIN_VALUE, Byte.MAX_VALUE})
    void byteRoundTrip(final byte value) {
        this.writer().writeByte(value);

        assertThat(this.reader().readByte()).isEqualTo(value);
    }

    @ParameterizedTest
    @ValueSource(shorts = {0, 1, -1, 0x1234, Short.MIN_VALUE, Short.MAX_VALUE})
    void shortRoundTrip(final short value) {
        this.writer().writeShort(value);
        this.writer().writeShortLE(value);

        assertThat(this.reader().readShort()).isEqualTo(value);
        assertThat(this.reader().readShortLE()).isEqualTo(value);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, -1, 0x12345678, Integer.MIN_VALUE, Integer.MAX_VALUE})
    void intRoundTrip(final int value) {
        this.writer().writeInt(value);
        this.writer().writeIntLE(value);

        assertThat(this.reader().readInt()).isEqualTo(value);
        assertThat(this.reader().readIntLE()).isEqualTo(value);
    }

    @ParameterizedTest
    @ValueSource(longs = {0, 1, -1, 0x1234567890ABCDEFL, Long.MIN_VALUE, Long.MAX_VALUE})
    void longRoundTrip(final long value) {
        this.writer().writeLong(value);
        this.writer().writeLongLE(value);

        assertThat(this.reader().readLong()).isEqualTo(value);
        assertThat(this.reader().readLongLE()).isEqualTo(value);
    }

    @ParameterizedTest
    @ValueSource(floats = {0f, -0f, 1f, 123.456f, Float.MIN_VALUE, Float.MAX_VALUE, Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY})
    void floatRoundTrip(final float value) {
        this.writer().writeFloat(value);
        this.writer().writeFloatLE(value);

        assertThat(Float.floatToRawIntBits(this.reader().readFloat())).isEqualTo(Float.floatToRawIntBits(value));
        assertThat(Float.floatToRawIntBits(this.reader().readFloatLE())).isEqualTo(Float.floatToRawIntBits(value));
    }

    @ParameterizedTest
    @ValueSource(doubles = {0d, -0d, 1d, 123.456, Double.MIN_VALUE, Double.MAX_VALUE, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY})
    void doubleRoundTrip(final double value) {
        this.writer().writeDouble(value);
        this.writer().writeDoubleLE(value);

        assertThat(Double.doubleToRawLongBits(this.reader().readDouble())).isEqualTo(Double.doubleToRawLongBits(value));
        assertThat(Double.doubleToRawLongBits(this.reader().readDoubleLE())).isEqualTo(Double.doubleToRawLongBits(value));
    }

    @Test
    void shortByteOrder() {
        this.writer().writeShort((short) 0x1234);
        this.writer().writeShortLE((short) 0x1234);

        assertThat(this.readBytes(4)).containsExactly(0x12, 0x34, 0x34, 0x12);
    }

    @Test
    void intByteOrder() {
        this.writer().writeInt(0x12345678);
        this.writer().writeIntLE(0x12345678);

        assertThat(this.readBytes(8)).containsExactly(
                0x12, 0x34, 0x56, 0x78,
                0x78, 0x56, 0x34, 0x12);
    }

    @Test
    void longByteOrder() {
        this.writer().writeLong(0x1234567890ABCDEFL);
        this.writer().writeLongLE(0x1234567890ABCDEFL);

        assertThat(this.readBytes(16)).containsExactly(
                0x12, 0x34, 0x56, 0x78, 0x90, 0xAB, 0xCD, 0xEF,
                0xEF, 0xCD, 0xAB, 0x90, 0x78, 0x56, 0x34, 0x12);
    }

    @Test
    void floatAndDoubleAreBigEndian() {
        this.writer().writeFloat(1f);
        this.writer().writeDouble(1d);

        assertThat(this.readBytes(12)).containsExactly(
                0x3F, 0x80, 0x00, 0x00,
                0x3F, 0xF0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00);
    }

    @Test
    void floatAndDoubleLittleEndianByteOrder() {
        this.writer().writeFloatLE(1f);
        this.writer().writeDoubleLE(1d);

        assertThat(this.readBytes(12)).containsExactly(
                0x00, 0x00, 0x80, 0x3F,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0xF0, 0x3F);
    }

    @Test
    void bytesRoundTrip() {
        final byte[] source = {0x01, 0x02, 0x03, (byte) 0xFF};
        this.writer().writeBytes(source);

        final byte[] destination = new byte[source.length];
        this.reader().readBytes(destination);

        assertThat(destination).containsExactly(source);
    }

    @Test
    void emptyBytes() {
        this.writer().writeBytes(new byte[0]);
        this.reader().readBytes(new byte[0]);

        assertThat(this.reader().remaining()).isZero();
    }

    @Test
    void remainingIsZeroWhenNothingWritten() {
        assertThat(this.reader().remaining()).isZero();
    }

    @Test
    void remainingTracksReads() {
        this.writer().writeByte((byte) 1);
        this.writer().writeInt(2);
        this.writer().writeLong(3);
        assertThat(this.reader().remaining()).isEqualTo(13);

        this.reader().readByte();
        assertThat(this.reader().remaining()).isEqualTo(12);

        this.reader().readInt();
        this.reader().readLong();
        assertThat(this.reader().remaining()).isZero();
    }

    @Test
    void mixedSequenceReadsBackInOrder() {
        this.writer().writeByte((byte) 0x12);
        this.writer().writeShortLE((short) 0x1234);
        this.writer().writeInt(0x12345678);
        this.writer().writeDouble(123.456);
        this.writer().writeBytes(new byte[]{1, 2, 3});

        assertThat(this.reader().readByte()).isEqualTo((byte) 0x12);
        assertThat(this.reader().readShortLE()).isEqualTo((short) 0x1234);
        assertThat(this.reader().readInt()).isEqualTo(0x12345678);
        assertThat(this.reader().readDouble()).isEqualTo(123.456);
        final byte[] bytes = new byte[3];
        this.reader().readBytes(bytes);
        assertThat(bytes).containsExactly(1, 2, 3);
        assertThat(this.reader().remaining()).isZero();
    }

    @Test
    void readingPastEndThrowsUnderflow() {
        this.writer().writeByte((byte) 1);

        assertThatThrownBy(() -> this.reader().readInt()).isInstanceOf(ReadUnderflowException.class);
    }

    @Test
    void readingBytesPastEndThrowsUnderflow() {
        this.writer().writeByte((byte) 1);

        assertThatThrownBy(() -> this.reader().readBytes(new byte[2])).isInstanceOf(ReadUnderflowException.class);
    }

    private int[] readBytes(final int count) {
        final int[] bytes = new int[count];
        for (int i = 0; i < count; i++) {
            bytes[i] = this.reader().readByte() & 0xFF;
        }
        assertThat(this.reader().remaining()).isZero();
        return bytes;
    }

}
