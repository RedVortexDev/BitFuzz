package red.vortx.bitfuzz.fuzz.core.runner;

import red.vortx.bitfuzz.buffer.nio.NIOByteReader;
import red.vortx.bitfuzz.fuzz.api.FuzzFailure;
import red.vortx.bitfuzz.fuzz.api.FuzzResult;
import red.vortx.bitfuzz.fuzz.api.FuzzRunner;
import red.vortx.bitfuzz.fuzz.api.GeneratorLookup;
import red.vortx.bitfuzz.fuzz.core.Bytes;
import red.vortx.bitfuzz.fuzz.core.generator.impl.SchemaGenerator;
import red.vortx.bitfuzz.schema.api.Schema;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.random.RandomGenerator;

/// Checks that every generated value survives being encoded and decoded. The decoded values must equal the
/// original ones, and decoding must consume exactly the encoded bytes.
///
/// Failures aren't shrunk. If encoding throws, the failure's input is empty.
///
/// @param schema      the schema under test
/// @param generator   the generator for the schema's values
/// @param maxFailures the number of failures after which the run stops early
public record RoundTripRunner(Schema schema, SchemaGenerator generator, int maxFailures) implements FuzzRunner {

    public static final int DEFAULT_MAX_FAILURES = 10;

    public RoundTripRunner {
        Objects.requireNonNull(schema, "schema");
        Objects.requireNonNull(generator, "generator");

        if (maxFailures < 1) {
            throw new IllegalArgumentException("maxFailures must be at least 1");
        }
    }

    /// Creates a runner that generates values with the given lookup.
    ///
    /// @param schema the schema under test
    /// @param lookup where to find generators for the field types
    /// @throws IllegalArgumentException if a field's type has no generator
    public RoundTripRunner(final Schema schema, final GeneratorLookup lookup) {
        this(schema, SchemaGenerator.of(schema, lookup), DEFAULT_MAX_FAILURES);
    }

    @Override
    public FuzzResult run(final int iterations, final long seed) {
        final CaseSeeds seeds = new CaseSeeds(seed);
        final List<FuzzFailure> failures = new ArrayList<>();

        int iteration;
        for (iteration = 0; iteration < iterations && failures.size() < this.maxFailures; iteration++) {
            final long caseSeed = seeds.next();
            final RandomGenerator random = CaseSeeds.random(caseSeed);
            final List<Object> values = this.generator.generate(random);

            byte[] bytes = new byte[0];
            try {
                bytes = Bytes.encode(this.schema, values);
                this.check(values, bytes);
            } catch (final Throwable t) {
                failures.add(new FuzzFailure(caseSeed, iteration, bytes, bytes, t));
            }
        }

        return new FuzzResult(iteration, failures);
    }

    private void check(final List<Object> values, final byte[] bytes) {
        final NIOByteReader reader = new NIOByteReader(ByteBuffer.wrap(bytes));
        final List<Object> decoded = this.schema.read(reader);

        if (!decoded.equals(values)) {
            throw new AssertionError("decoded %s but wrote %s".formatted(decoded, values));
        }
        if (reader.remaining() != 0) {
            throw new AssertionError("%d of %d bytes were left unread".formatted(reader.remaining(), bytes.length));
        }
    }

}
