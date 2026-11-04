package red.vortx.bitfuzz.fuzz.core.generator;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import red.vortx.bitfuzz.fuzz.api.Generator;
import red.vortx.bitfuzz.fuzz.api.GeneratorLookup;
import red.vortx.bitfuzz.fuzz.api.GeneratorProvider;
import red.vortx.bitfuzz.fuzz.core.Bytes;
import red.vortx.bitfuzz.fuzz.core.generator.provider.ContainerProvider;
import red.vortx.bitfuzz.schema.api.Schema;
import red.vortx.bitfuzz.schema.api.field.type.SimpleType;
import red.vortx.bitfuzz.schema.api.field.type.Type;
import red.vortx.bitfuzz.schema.api.field.type.TypeSpec;
import red.vortx.bitfuzz.schema.std.Types;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static red.vortx.bitfuzz.fuzz.core.generator.Samples.random;
import static red.vortx.bitfuzz.fuzz.core.generator.Samples.resolve;
import static red.vortx.bitfuzz.fuzz.core.generator.Samples.sample;
import static red.vortx.bitfuzz.fuzz.core.generator.Samples.single;

class GeneratorsTest {

    private static final Generators REGISTRY = Generators.standard();

    static Stream<TypeSpec<?>> everyStandardType() {
        return Stream.of(
                Types.BYTE, Types.SHORT, Types.SHORT_LE, Types.INT, Types.INT_LE, Types.LONG, Types.LONG_LE,
                Types.FLOAT, Types.FLOAT_LE, Types.DOUBLE, Types.DOUBLE_LE,
                Types.BOOL, Types.UUID, Types.STRING, Types.string(Types.VAR_INT), Types.VAR_INT, Types.VAR_LONG,
                Types.list(Types.INT), Types.list(Types.STRING, Types.VAR_INT),
                Types.map(Types.STRING, Types.LONG), Types.map(Types.BOOL, Types.UUID),
                Types.optional(Types.VAR_INT),
                Types.list(Types.optional(Types.map(Types.VAR_LONG, Types.list(Types.STRING))))
        );
    }

    @ParameterizedTest
    @MethodSource("everyStandardType")
    void generatedValuesRoundTrip(final TypeSpec<?> spec) {
        final Schema schema = single(spec);
        final Generator<?> generator = REGISTRY.ofType(schema.fields().getFirst().type());

        for (final Object value : sample(generator, random(), 500)) {
            final List<Object> values = List.of(value);

            assertThat(Bytes.decode(schema, Bytes.encode(schema, values))).isEqualTo(values);
        }
    }

    @ParameterizedTest
    @MethodSource("everyStandardType")
    void generatedValuesMatchJavaType(final TypeSpec<?> spec) {
        final Type<?> type = resolve(spec);

        assertThat(sample(REGISTRY.ofType(type), random(), 200)).allSatisfy(value ->
                assertThat(value).isInstanceOf(type.javaType()));
    }

    @Test
    void sameSeedGivesSameValues() {
        final Generator<?> generator = REGISTRY.ofType(resolve(Types.map(Types.STRING, Types.list(Types.LONG))));

        assertThat(sample(generator, random(7), 100)).isEqualTo(sample(generator, random(7), 100));
        assertThat(sample(generator, random(7), 100)).isNotEqualTo(sample(generator, random(8), 100));
    }

    @Test
    void unknownTypeFailsLoudly() {
        final Type<StringBuilder> custom = new SimpleType<>(StringBuilder.class, (out, value) -> {
        }, in -> new StringBuilder());

        assertThatThrownBy(() -> REGISTRY.ofType(custom))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("StringBuilder");
    }

    @Test
    void unknownTypeInsideContainerFailsLoudly() {
        final Type<StringBuilder> custom = new SimpleType<>(StringBuilder.class, (out, value) -> {
        }, in -> new StringBuilder());

        assertThatThrownBy(() -> REGISTRY.ofType(resolve(Types.list(custom))))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void customProviderAddsSupportForNewTypes() {
        final Type<StringBuilder> custom = new SimpleType<>(StringBuilder.class, (out, value) -> {
        }, in -> new StringBuilder());
        final GeneratorProvider provider = new GeneratorProvider() {
            @Override
            public <T> Optional<Generator<T>> create(final @NonNull Type<T> type, final @NonNull GeneratorLookup lookup) {
                //noinspection unchecked
                return type == custom
                        ? Optional.of((Generator<T>) (Generator<StringBuilder>) random -> new StringBuilder("custom"))
                        : Optional.empty();
            }
        };

        final Generators registry = REGISTRY.with(provider);

        assertThat(registry.ofType(custom).generate(random()).toString()).isEqualTo("custom");
        assertThat(sample(registry.ofType(resolve(Types.list(custom))), random(), 50))
                .allSatisfy(list -> assertThat(list).allSatisfy(element ->
                        assertThat(element.toString()).isEqualTo("custom")));
    }

    @Test
    void customProviderOverridesStandardGenerators() {
        final GeneratorProvider alwaysSeven = new GeneratorProvider() {
            @Override
            public <T> Optional<Generator<T>> create(final @NonNull Type<T> type, final @NonNull GeneratorLookup lookup) {
                //noinspection unchecked
                return type.javaType() == Integer.class
                        ? Optional.of((Generator<T>) (Generator<Integer>) random -> 7)
                        : Optional.empty();
            }
        };

        final Generators registry = REGISTRY.with(alwaysSeven);

        assertThat(sample(registry.ofType(resolve(Types.VAR_INT)), random(), 50)).containsOnly(7);
        assertThat(sample(REGISTRY.ofType(resolve(Types.VAR_INT)), random(), 50)).doesNotContainSequence(7, 7, 7);
    }

    @Test
    void withDoesNotChangeOriginal() {
        final Generators extended = REGISTRY.with(new ContainerProvider());

        assertThat(extended.providers()).hasSize(REGISTRY.providers().size() + 1);
        assertThat(REGISTRY.providers()).hasSize(2);
    }

}
