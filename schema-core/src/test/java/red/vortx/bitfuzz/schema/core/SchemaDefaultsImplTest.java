package red.vortx.bitfuzz.schema.core;

import org.junit.jupiter.api.Test;
import red.vortx.bitfuzz.schema.api.Endianness;
import red.vortx.bitfuzz.schema.api.SchemaDefaults;
import red.vortx.bitfuzz.schema.api.field.type.EndiannessTypeSpec;

import static org.assertj.core.api.Assertions.assertThat;

class SchemaDefaultsImplTest {

    @Test
    void toMutableCopiesValues() {
        final SchemaDefaults defaults = new SchemaDefaultsImpl(Endianness.LITTLE_ENDIAN, Primitives.INT_LE);
        final SchemaDefaults.Mutable mutable = defaults.toMutable();

        assertThat(mutable.endianness()).isEqualTo(Endianness.LITTLE_ENDIAN);
        assertThat(mutable.lengthType()).isSameAs(Primitives.INT_LE);
    }

    @Test
    void mutatingCopyDoesNotChangeOriginal() {
        final SchemaDefaults defaults = new SchemaDefaultsImpl(Endianness.BIG_ENDIAN, Primitives.INT);

        defaults.toMutable().endianness(Endianness.LITTLE_ENDIAN).lengthType(Primitives.INT_LE);

        assertThat(defaults.endianness()).isEqualTo(Endianness.BIG_ENDIAN);
        assertThat(defaults.lengthType()).isSameAs(Primitives.INT);
    }

    @Test
    void buildSnapshotsCurrentValues() {
        final SchemaDefaults.Mutable mutable = new SchemaDefaultsImpl(Endianness.BIG_ENDIAN, Primitives.INT).toMutable();
        final SchemaDefaults built = mutable.build();

        mutable.endianness(Endianness.LITTLE_ENDIAN);

        assertThat(built.endianness()).isEqualTo(Endianness.BIG_ENDIAN);
    }

    @Test
    void lengthSpecResolvesWithCurrentEndianness() {
        final SchemaDefaults.Mutable mutable = new SchemaDefaultsImpl(Endianness.BIG_ENDIAN, Primitives.INT).toMutable()
                .lengthType(new EndiannessTypeSpec<>(Primitives.INT, Primitives.INT_LE));

        assertThat(mutable.lengthType()).isSameAs(Primitives.INT);

        mutable.endianness(Endianness.LITTLE_ENDIAN);

        assertThat(mutable.lengthType()).isSameAs(Primitives.INT_LE);
        assertThat(mutable.build().lengthType()).isSameAs(Primitives.INT_LE);
    }

    @Test
    void endiannessOpposite() {
        assertThat(Endianness.BIG_ENDIAN.opposite()).isEqualTo(Endianness.LITTLE_ENDIAN);
        assertThat(Endianness.LITTLE_ENDIAN.opposite()).isEqualTo(Endianness.BIG_ENDIAN);
        assertThat(Endianness.BIG_ENDIAN.isBigEndian()).isTrue();
        assertThat(Endianness.BIG_ENDIAN.isLittleEndian()).isFalse();
        assertThat(Endianness.LITTLE_ENDIAN.isLittleEndian()).isTrue();
        assertThat(Endianness.LITTLE_ENDIAN.isBigEndian()).isFalse();
    }

}
