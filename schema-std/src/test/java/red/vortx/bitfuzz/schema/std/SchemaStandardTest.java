package red.vortx.bitfuzz.schema.std;

import org.junit.jupiter.api.Test;
import red.vortx.bitfuzz.schema.api.Endianness;
import red.vortx.bitfuzz.schema.api.Schema;
import red.vortx.bitfuzz.schema.core.SchemaBuilderImpl;

import static org.assertj.core.api.Assertions.assertThat;

public class SchemaStandardTest {

    @Test
    void testIdentity() {
        final Schema schema = new SchemaBuilderImpl()
                .defaults(defaults -> defaults
                        .endianness(Endianness.LITTLE_ENDIAN)
                        .lengthType(Types.INT)
                )
                .field(Types.INT, "follows schema")
                .field(Types.INT_BE, "big endian")
                .build();

        assertThat(schema.fields().getFirst().type().equals(Types.INT_LE)).isTrue();
        assertThat(schema.fields().get(1).type().equals(Types.INT_BE)).isTrue();
    }

}
