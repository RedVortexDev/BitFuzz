package red.vortx.bitfuzz.fuzz.core;

import org.junit.jupiter.api.Test;
import red.vortx.bitfuzz.fuzz.core.generator.Generators;
import red.vortx.bitfuzz.fuzz.core.generator.impl.SchemaGenerator;
import red.vortx.bitfuzz.schema.api.Schema;
import red.vortx.bitfuzz.schema.api.SchemaBuilder;
import red.vortx.bitfuzz.schema.std.type.Types;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class WireDumpTest {

    record Point(int x, int y) {
    }

    record Profile(String name, List<Integer> scores, Optional<Point> home, Map<String, Boolean> flags) {
    }

    private static final Schema PLAYER = SchemaBuilder.create()
            .field(Types.STRING, "name")
            .field(Types.INT, "level")
            .field(Types.BOOL, "online")
            .build();

    private static final Schema EVERYTHING = SchemaBuilder.create()
            .field(Types.record(Profile.class), "profile")
            .field(Types.map(Types.STRING, Types.list(Types.optional(Types.VAR_LONG))), "nested")
            .field(Types.UUID, "id")
            .build();

    @Test
    void marksEveryByteWithItsValue() {
        assertThat(WireDump.explain(PLAYER, List.of("steve", 12, true))).isEqualTo("""
                05 73 74 65 76 65 00 00 00 0c 01
                ╰╮ ╰──────┬─────╯ ╰────┬────╯ ╰╮
                 │        │            │       ╰─ online: true
                 │        │            ╰─ level: 12
                 │        ╰─ name: "steve"
                 ╰─ name.length: 5
                """);
    }

    @Test
    void splitsLongEncodingsIntoRows() {
        final Schema schema = SchemaBuilder.create()
                .field(Types.record(Profile.class), "profile")
                .build();
        final Profile profile = new Profile("alexander", List.of(1, 300, 7), Optional.of(new Point(1, 2)), Map.of());

        assertThat(WireDump.explain(schema, List.of(profile))).isEqualTo("""
                09 61 6c 65 78 61 6e 64 65 72 03 00 00 00 01 00
                ╰╮ ╰────────────┬───────────╯ ╰╮ ╰────┬────╯ ╰┬
                 │              │              │      │       ╰─ profile.scores[1]: 300
                 │              │              │      ╰─ profile.scores[0]: 1
                 │              │              ╰─ profile.scores.length: 3
                 │              ╰─ profile.name: "alexander"
                 ╰─ profile.name.length: 9

                00 01 2c 00 00 00 07 01 00 00 00 01 00 00 00 02
                ───────╯ ╰────┬────╯ ╰╮ ╰────┬────╯ ╰────┬────╯
                              │       │      │           ╰─ profile.home.y: 2
                              │       │      ╰─ profile.home.x: 1
                              │       ╰─ profile.home.present: true
                              ╰─ profile.scores[2]: 7

                00
                ╰╮
                 ╰─ profile.flags.length: 0
                """);
    }

    @Test
    void explainsNestedValuesDownToTheirLeaves() {
        final Schema schema = SchemaBuilder.create()
                .field(Types.record(Profile.class), "profile")
                .build();
        final Profile profile = new Profile("al", List.of(1, 300), Optional.of(new Point(1, 2)), Map.of("admin", true));

        assertThat(WireDump.explain(schema, List.of(profile)))
                .contains("profile.name.length: 2")
                .contains("profile.scores[1]: 300")
                .contains("profile.home.present: true")
                .contains("profile.home.y: 2")
                .contains("profile.flags[0].key: \"admin\"")
                .contains("profile.flags[0].value: true");
    }

    @Test
    void explainsAnyGeneratedValue() {
        final SchemaGenerator generator = SchemaGenerator.of(EVERYTHING, Generators.standard());
        final RandomGenerator random = RandomGeneratorFactory.of("L64X128MixRandom").create(42);

        for (int i = 0; i < 500; i++) {
            final List<Object> values = generator.generate(random);
            final String hexRows = Arrays.stream(WireDump.explain(EVERYTHING, values).split("\n\n"))
                    .map(row -> row.lines().findFirst().orElseThrow())
                    .collect(Collectors.joining(" "));

            assertThat(hexRows).isEqualTo(Bytes.hex(Bytes.encode(EVERYTHING, values)));
        }
    }

}
