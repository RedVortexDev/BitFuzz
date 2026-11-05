package red.vortx.bitfuzz.fuzz.core.generator.impl;

import red.vortx.bitfuzz.fuzz.api.Generator;

import java.util.List;
import java.util.random.RandomGenerator;

/// Generates strings mixing ASCII and 2 to 4 byte UTF-8 characters, plus edge-case strings.
public record StringGenerator() implements Generator<String> {

    private static final List<String> EDGES = List.of("", " ", "\0", "é", "日本語", "😀", "\uFEFF", "a\u0301");
    private static final int MAX_SHORT_LENGTH = 16;
    private static final int MAX_LONG_LENGTH = 4096;

    @Override
    public String generate(final RandomGenerator random) {
        return switch (random.nextInt(8)) {
            case 0 -> EDGES.get(random.nextInt(EDGES.size()));
            case 1 -> this.randomString(random, random.nextInt(MAX_LONG_LENGTH));
            default -> this.randomString(random, random.nextInt(MAX_SHORT_LENGTH + 1));
        };
    }

    private String randomString(final RandomGenerator random, final int codePoints) {
        final StringBuilder builder = new StringBuilder(codePoints);

        for (int i = 0; i < codePoints; i++) {
            builder.appendCodePoint(this.codePoint(random));
        }

        return builder.toString();
    }

    private int codePoint(final RandomGenerator random) {
        return switch (random.nextInt(4)) {
            case 0, 1 -> random.nextInt(0x20, 0x7F);
            case 2 -> random.nextBoolean()
                    ? random.nextInt(0xA0, 0x800)
                    : random.nextInt(0x4E00, 0xA000);
            default -> random.nextInt(0x1F300, 0x1FAFF);
        };
    }

}
