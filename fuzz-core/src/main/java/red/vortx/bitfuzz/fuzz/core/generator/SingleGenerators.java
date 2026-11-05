package red.vortx.bitfuzz.fuzz.core.generator;

import red.vortx.bitfuzz.fuzz.api.Generator;
import red.vortx.bitfuzz.fuzz.core.generator.impl.BiasedGenerator;

import java.util.List;
import java.util.UUID;
import java.util.random.RandomGenerator;

/// Generators for single values.
public enum SingleGenerators {
    ;

    public static final Generator<Byte> BYTE = new BiasedGenerator<>(
            List.of((byte) 0, (byte) 1, (byte) -1, Byte.MIN_VALUE, Byte.MAX_VALUE),
            random -> (byte) random.nextInt(-16, 16),
            random -> (byte) random.nextInt()
    );

    public static final Generator<Short> SHORT = new BiasedGenerator<>(
            List.of((short) 0, (short) 1, (short) -1, (short) 127, (short) 128, (short) 255, (short) 256,
                    Short.MIN_VALUE, Short.MAX_VALUE
            ),
            random -> (short) random.nextInt(-1000, 1000),
            random -> (short) random.nextInt()
    );

    public static final Generator<Integer> INT = new BiasedGenerator<>(
            List.of(0, 1, -1, 255, 256,
                    Integer.MIN_VALUE, Integer.MAX_VALUE,
                    (1 << 7) - 1, 1 << 7,
                    (1 << 14) - 1, 1 << 14,
                    (1 << 21) - 1, 1 << 21,
                    (1 << 28) - 1, 1 << 28
            ),
            random -> random.nextInt(-1000, 1000),
            RandomGenerator::nextInt
    );

    public static final Generator<Long> LONG = new BiasedGenerator<>(
            List.of(0L, 1L, -1L,
                    (long) Integer.MIN_VALUE, (long) Integer.MAX_VALUE, Long.MIN_VALUE, Long.MAX_VALUE,
                    (1L << 7) - 1, 1L << 7,
                    (1L << 14) - 1, 1L << 14,
                    (1L << 35) - 1, 1L << 35,
                    (1L << 56) - 1, 1L << 56,
                    1L << 32
            ),
            random -> random.nextLong(-1000, 1000),
            RandomGenerator::nextLong
    );

    public static final Generator<Float> FLOAT = new BiasedGenerator<>(
            List.of(0f, -0f, 1f, -1f,
                    Float.MIN_VALUE, Float.MAX_VALUE, -Float.MAX_VALUE,
                    Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY
            ),
            random -> random.nextFloat(-1000f, 1000f),
            random -> Float.intBitsToFloat(random.nextInt())
    );

    public static final Generator<Double> DOUBLE = new BiasedGenerator<>(
            List.of(0d, -0d, 1d, -1d,
                    Double.MIN_VALUE, Double.MAX_VALUE, -Double.MAX_VALUE,
                    Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY
            ),
            random -> random.nextDouble(-1000d, 1000d),
            random -> Double.longBitsToDouble(random.nextLong())
    );

    public static final Generator<Boolean> BOOLEAN = RandomGenerator::nextBoolean;

    public static final Generator<UUID> UUID_VALUE = new BiasedGenerator<>(
            List.of(new UUID(0, 0), new UUID(-1, -1),
                    new UUID(Long.MIN_VALUE, Long.MAX_VALUE)
            ),
            random -> {
                // version 4, IETF variant
                final long mostSigBits = (random.nextLong() & 0xFFFFFFFFFFFF0FFFL) | 0x0000000000004000L;
                final long leastSigBits = (random.nextLong() & 0x3FFFFFFFFFFFFFFFL) | 0x8000000000000000L;

                return new UUID(mostSigBits, leastSigBits);
            },
            random -> new UUID(random.nextLong(), random.nextLong())
    );

}
