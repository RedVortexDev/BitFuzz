package red.vortx.bitfuzz.schema.std;

import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import red.vortx.bitfuzz.fuzz.api.FuzzResult;
import red.vortx.bitfuzz.fuzz.core.Fuzz;
import red.vortx.bitfuzz.schema.api.Schema;
import red.vortx.bitfuzz.schema.api.SchemaBuilder;
import red.vortx.bitfuzz.schema.api.SchemaException;
import red.vortx.bitfuzz.schema.std.type.Types;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/// Fuzzes realistic schemas built from every standard type.
///
/// - Round trip: random valid values must decode to themselves and consume every byte.
/// - Mutation: damaged encodings must decode or be rejected with [SchemaException], never crash.
///
/// Set `-Dbitfuzz.seed=<n>` to try other seeds locally; the default keeps runs reproducible.
@Timeout(120)
class StandardTypesFuzzTest {

    private static final long SEED = Long.getLong("bitfuzz.seed", 42L);

    static Stream<Schema> schemas() {
        return Stream.of(
                SchemaBuilder.create()
                        .field(Types.VAR_INT, "id")
                        .field(Types.STRING, "name")
                        .field(Types.list(Types.optional(Types.UUID)), "friends")
                        .field(Types.map(Types.STRING, Types.LONG), "stats")
                        .field(Types.BOOL, "online")
                        .build(),
                SchemaBuilder.create()
                        .field(Types.list(Types.map(Types.VAR_LONG, Types.list(Types.STRING))), "nested")
                        .build(),
                SchemaBuilder.create()
                        .field(Types.optional(Types.list(Types.optional(Types.string(Types.VAR_INT)))), "deep")
                        .field(Types.map(Types.BOOL, Types.DOUBLE), "flags")
                        .build(),
                SchemaBuilder.create()
                        .field(Types.SHORT, "short")
                        .field(Types.INT_LE, "int")
                        .field(Types.LONG, "long")
                        .field(Types.FLOAT, "float")
                        .field(Types.DOUBLE_LE, "double")
                        .field(Types.VAR_INT, "varint")
                        .field(Types.VAR_LONG, "varlong")
                        .field(Types.BYTE, "byte")
                        .build()
        );
    }

    @ParameterizedTest
    @MethodSource("schemas")
    void roundTrip(final Schema schema) {
        final FuzzResult result = Fuzz.roundTrip(schema, 10_000, SEED);

        assertThat(result.isClean()).as(result.report()).isTrue();
    }

    @ParameterizedTest
    @MethodSource("schemas")
    void survivesMutation(final Schema schema) {
        final FuzzResult result = Fuzz.mutate(schema, 25_000, SEED);

        assertThat(result.isClean()).as(result.report()).isTrue();
    }

}
