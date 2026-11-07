package red.vortx.bitfuzz.schema.std;

import org.junit.jupiter.api.Test;
import red.vortx.bitfuzz.schema.std.type.*;

import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class NullSafetyTest {

    @Test
    void typeRecordsRejectNulls() {
        assertThatNullPointerException()
                .isThrownBy(() -> new ListType<>(null, Types.INT_BE))
                .withMessage("elementType");
        assertThatNullPointerException().isThrownBy(() -> new ListType<>(Types.INT_BE, null)).withMessage("lengthType");
        assertThatNullPointerException()
                .isThrownBy(() -> new MapType<>(null, Types.INT_BE, Types.INT_BE))
                .withMessage("keyType");
        assertThatNullPointerException().isThrownBy(() -> new OptionalType<>(null)).withMessage("valueType");
        assertThatNullPointerException().isThrownBy(() -> new StringType(null)).withMessage("lengthType");
    }

    @Test
    void factoriesRejectNullsImmediately() {
        assertThatNullPointerException().isThrownBy(() -> Types.list(null)).withMessage("elementType");
        assertThatNullPointerException().isThrownBy(() -> Types.list(Types.INT, null)).withMessage("lengthType");
        assertThatNullPointerException().isThrownBy(() -> Types.map(Types.INT, null)).withMessage("valueType");
        assertThatNullPointerException().isThrownBy(() -> Types.optional(null)).withMessage("valueType");
        assertThatNullPointerException().isThrownBy(() -> Types.string(null)).withMessage("lengthType");
    }

}
