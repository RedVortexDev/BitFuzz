package red.vortx.bitfuzz.fuzz.core;

import org.junit.jupiter.api.Test;
import red.vortx.bitfuzz.schema.api.Schema;
import red.vortx.bitfuzz.schema.api.SchemaBuilder;
import red.vortx.bitfuzz.schema.std.Types;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BytesTest {

    private static final Schema SCHEMA = SchemaBuilder.create()
            .field(Types.VAR_INT, "id")
            .field(Types.STRING, "name")
            .build();

    @Test
    void encodeIsExactlySized() {
        assertThat(Bytes.encode(SCHEMA, List.of(300, "hi"))).hasSize(2 + 4 + 2);
    }

    @Test
    void roundTrip() {
        final List<Object> values = List.of(-1, "日本語");

        assertThat(Bytes.decode(SCHEMA, Bytes.encode(SCHEMA, values))).isEqualTo(values);
    }

}
