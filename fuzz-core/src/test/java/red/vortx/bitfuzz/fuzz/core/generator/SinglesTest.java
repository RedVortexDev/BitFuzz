package red.vortx.bitfuzz.fuzz.core.generator;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static red.vortx.bitfuzz.fuzz.core.generator.Samples.sample;

class SinglesTest {

    @Test
    void intsHitEdges() {
        final List<Integer> values = sample(SingleGenerators.INT);

        assertThat(values).contains(0, 1, -1, 127, 128, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    @Test
    void intsAreNotOnlyEdgesOrSmall() {
        final List<Integer> values = sample(SingleGenerators.INT);

        assertThat(values).anyMatch(value -> Math.abs((long) value) > 1_000_000 && value != Integer.MIN_VALUE
                && value != Integer.MAX_VALUE);
        assertThat(values).anyMatch(value -> value > -1000 && value < 1000 && value != 0 && value != 1 && value != -1);
    }

    @Test
    void longsHitEdges() {
        assertThat(sample(SingleGenerators.LONG)).contains(0L, -1L, Long.MIN_VALUE, Long.MAX_VALUE, 1L << 56);
    }

    @Test
    void bytesAndShortsHitEdges() {
        assertThat(sample(SingleGenerators.BYTE)).contains((byte) 0, Byte.MIN_VALUE, Byte.MAX_VALUE);
        assertThat(sample(SingleGenerators.SHORT)).contains((short) 0, (short) 128, Short.MIN_VALUE, Short.MAX_VALUE);
    }

    @Test
    void floatsIncludeSpecialValues() {
        final List<Float> values = sample(SingleGenerators.FLOAT);

        assertThat(values).contains(Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.MAX_VALUE);
        assertThat(values).anyMatch(value -> Float.floatToRawIntBits(value) == Float.floatToRawIntBits(-0f));
    }

    @Test
    void doublesIncludeSpecialValues() {
        final List<Double> values = sample(SingleGenerators.DOUBLE);

        assertThat(values).contains(Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY);
        assertThat(values).anyMatch(value -> Double.doubleToRawLongBits(value) == Double.doubleToRawLongBits(-0d));
    }

    @Test
    void booleansAreBoth() {
        assertThat(sample(SingleGenerators.BOOLEAN)).contains(true, false);
    }

    @Test
    void uuidsIncludeEdges() {
        assertThat(sample(SingleGenerators.UUID_VALUE)).contains(new UUID(0, 0), new UUID(-1, -1));
    }

}
