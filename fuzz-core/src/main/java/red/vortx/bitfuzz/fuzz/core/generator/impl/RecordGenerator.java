package red.vortx.bitfuzz.fuzz.core.generator.impl;

import red.vortx.bitfuzz.fuzz.api.Generator;
import red.vortx.bitfuzz.schema.api.SchemaException;
import red.vortx.bitfuzz.schema.std.record.RecordType;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.random.RandomGenerator;

/// Generates records by generating each component and calling the record's constructor.
///
/// Values the record's constructor rejects are generated again, up to [#MAX_ATTEMPTS] times, then it throws
/// [IllegalStateException].
///
/// @param type       the record type
/// @param components the generator for each component, in declaration order
/// @param <R>        the record type
public record RecordGenerator<R extends Record>(RecordType<R> type,
                                                List<Generator<?>> components) implements Generator<R> {

    public static final int MAX_ATTEMPTS = 1_000;

    public RecordGenerator {
        Objects.requireNonNull(type, "type");
        components = List.copyOf(components);
    }

    @Override
    public R generate(final RandomGenerator random) {
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            final Optional<R> record = this.tryGenerate(random);
            if (record.isPresent()) {
                return record.get();
            }
        }

        throw new IllegalStateException("Could not generate a valid %s in %d attempts"
                .formatted(this.type.javaType().getSimpleName(), MAX_ATTEMPTS)
        );
    }

    private Optional<R> tryGenerate(final RandomGenerator random) {
        final Object[] values = new Object[this.components.size()];
        for (int i = 0; i < values.length; i++) {
            values[i] = this.components.get(i).generate(random);
        }

        try {
            return Optional.of(this.type.construct(values));
        } catch (final SchemaException _) {
            return Optional.empty();
        }
    }

}
