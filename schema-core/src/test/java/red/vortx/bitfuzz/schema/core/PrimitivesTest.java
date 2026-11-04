package red.vortx.bitfuzz.schema.core;

import org.junit.jupiter.api.Test;
import red.vortx.bitfuzz.schema.api.SchemaDefaults;
import red.vortx.bitfuzz.schema.api.field.type.Type;

import static org.assertj.core.api.Assertions.assertThat;
import static red.vortx.bitfuzz.schema.core.TestBuffers.reader;
import static red.vortx.bitfuzz.schema.core.TestBuffers.written;

class PrimitivesTest {

    private static <T> void assertEncodes(final Type<T> type, final T value, final int... expected) {
        final byte[] bytes = written(out -> type.write(out, value));
        final byte[] expectedBytes = new byte[expected.length];
        for (int i = 0; i < expected.length; i++) {
            expectedBytes[i] = (byte) expected[i];
        }

        assertThat(bytes).containsExactly(expectedBytes);
        assertThat(type.read(reader(bytes))).isEqualTo(value);
        assertThat(type.sizeOf(value)).isEqualTo(expected.length);
    }

    @Test
    void byteEncoding() {
        assertEncodes(Primitives.BYTE, (byte) 0x12, 0x12);
        assertEncodes(Primitives.BYTE, (byte) -1, 0xFF);
    }

    @Test
    void shortEncoding() {
        assertEncodes(Primitives.SHORT, (short) 0x1234, 0x12, 0x34);
        assertEncodes(Primitives.SHORT_LE, (short) 0x1234, 0x34, 0x12);
    }

    @Test
    void intEncoding() {
        assertEncodes(Primitives.INT, 0x12345678, 0x12, 0x34, 0x56, 0x78);
        assertEncodes(Primitives.INT_LE, 0x12345678, 0x78, 0x56, 0x34, 0x12);
    }

    @Test
    void longEncoding() {
        assertEncodes(Primitives.LONG, 0x1234567890ABCDEFL, 0x12, 0x34, 0x56, 0x78, 0x90, 0xAB, 0xCD, 0xEF);
        assertEncodes(Primitives.LONG_LE, 0x1234567890ABCDEFL, 0xEF, 0xCD, 0xAB, 0x90, 0x78, 0x56, 0x34, 0x12);
    }

    @Test
    void floatEncoding() {
        assertEncodes(Primitives.FLOAT, 1f, 0x3F, 0x80, 0x00, 0x00);
        assertEncodes(Primitives.FLOAT_LE, 1f, 0x00, 0x00, 0x80, 0x3F);
    }

    @Test
    void doubleEncoding() {
        assertEncodes(Primitives.DOUBLE, 1d, 0x3F, 0xF0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00);
        assertEncodes(Primitives.DOUBLE_LE, 1d, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0xF0, 0x3F);
    }

    @Test
    void javaTypes() {
        assertThat(Primitives.BYTE.javaType()).isEqualTo(Byte.class);
        assertThat(Primitives.SHORT.javaType()).isEqualTo(Short.class);
        assertThat(Primitives.SHORT_LE.javaType()).isEqualTo(Short.class);
        assertThat(Primitives.INT.javaType()).isEqualTo(Integer.class);
        assertThat(Primitives.INT_LE.javaType()).isEqualTo(Integer.class);
        assertThat(Primitives.LONG.javaType()).isEqualTo(Long.class);
        assertThat(Primitives.LONG_LE.javaType()).isEqualTo(Long.class);
        assertThat(Primitives.FLOAT.javaType()).isEqualTo(Float.class);
        assertThat(Primitives.FLOAT_LE.javaType()).isEqualTo(Float.class);
        assertThat(Primitives.DOUBLE.javaType()).isEqualTo(Double.class);
        assertThat(Primitives.DOUBLE_LE.javaType()).isEqualTo(Double.class);
    }

    @Test
    void typesResolveToThemselves() {
        final SchemaDefaults defaults = ((SchemaImpl) new SchemaBuilderImpl().build()).defaults();

        assertThat(Primitives.INT.resolve(defaults)).isSameAs(Primitives.INT);
    }

}
