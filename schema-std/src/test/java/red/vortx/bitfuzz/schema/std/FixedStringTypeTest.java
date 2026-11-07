package red.vortx.bitfuzz.schema.std;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import red.vortx.bitfuzz.fuzz.api.FuzzResult;
import red.vortx.bitfuzz.fuzz.core.Fuzz;
import red.vortx.bitfuzz.schema.api.Schema;
import red.vortx.bitfuzz.schema.api.SchemaException;
import red.vortx.bitfuzz.schema.std.type.Types;

import static org.assertj.core.api.Assertions.*;
import static red.vortx.bitfuzz.schema.std.TestSchemas.*;

class FixedStringTypeTest {

    @Test
    void writesOnlyTheBytes() {
        assertThat(encode(Types.fixedString(4), "BTZF")).containsExactly(bytes('B', 'T', 'Z', 'F'));
        assertThat(encode(Types.fixedString(0), "")).isEmpty();
    }

    @Test
    void lengthCountsBytesNotCharacters() {
        assertThat(encode(Types.fixedString(2), "é")).containsExactly(bytes(0xC3, 0xA9));
        assertRoundTrip(Types.fixedString(9), "日本語");
    }

    @Test
    void rejectsStringsOfAnotherLength() {
        assertThatThrownBy(() -> encode(Types.fixedString(4), "abc"))
                .isInstanceOf(SchemaException.class)
                .cause()
                .hasMessageContaining("4 bytes but is 3");
        assertThatThrownBy(() -> encode(Types.fixedString(1), "é")).isInstanceOf(SchemaException.class);
    }

    @Test
    void readsExactlyItsLength() {
        assertThat(decode(Types.fixedString(2), bytes('h', 'i'))).isEqualTo("hi");
        assertRejects(Types.fixedString(4), 'h', 'i');
        assertRejects(Types.fixedString(2), 0xC3, 0x28);
    }

    @Test
    void rejectsNegativeLength() {
        assertThatIllegalArgumentException().isThrownBy(() -> Types.fixedString(-1));
    }

    @Test
    @Timeout(60)
    void survivesFuzzing() {
        final Schema schema = single(Types.fixedString(12));

        final FuzzResult roundTrip = Fuzz.roundTrip(schema, 2_000, 42);
        final FuzzResult mutated = Fuzz.mutate(schema, 10_000, 42);

        assertThat(roundTrip.isClean()).as(roundTrip.report()).isTrue();
        assertThat(mutated.isClean()).as(mutated.report()).isTrue();
    }

}
