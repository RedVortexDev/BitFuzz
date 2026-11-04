package red.vortx.bitfuzz.schema.core;

import org.junit.jupiter.api.Test;
import red.vortx.bitfuzz.schema.api.Endianness;
import red.vortx.bitfuzz.schema.api.SchemaDefaults;

import static org.assertj.core.api.Assertions.assertThat;

class SchemaDefaultsInitialTest {

    @Test
    void initialIsBigEndianWithIntLength() {
        final SchemaDefaults.Mutable initial = SchemaDefaultsImpl.initial();

        assertThat(initial.endianness()).isEqualTo(Endianness.BIG_ENDIAN);
        assertThat(initial.lengthType()).isSameAs(Primitives.INT);
    }

    @Test
    void initialLengthFollowsEndianness() {
        final SchemaDefaults.Mutable initial = SchemaDefaultsImpl.initial().endianness(Endianness.LITTLE_ENDIAN);

        assertThat(initial.lengthType()).isSameAs(Primitives.INT_LE);
        assertThat(initial.build().lengthType()).isSameAs(Primitives.INT_LE);
    }

    @Test
    void initialReturnsIndependentCopies() {
        SchemaDefaultsImpl.initial().endianness(Endianness.LITTLE_ENDIAN);

        assertThat(SchemaDefaultsImpl.initial().endianness()).isEqualTo(Endianness.BIG_ENDIAN);
    }

}
