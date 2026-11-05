package red.vortx.bitfuzz.fuzz.core.generator.impl;

import red.vortx.bitfuzz.fuzz.api.Generator;

import java.util.random.RandomGenerator;

/// Generates fixed length strings mixing 1 to 4 byte characters.
///
/// @param byteLength the number of bytes
public record FixedStringGenerator(int byteLength) implements Generator<String> {

    @Override
    public String generate(final RandomGenerator random) {
        final StringBuilder builder = new StringBuilder();
        int remaining = this.byteLength;

        while (remaining > 0) {
            final int size = random.nextInt(1, Math.min(remaining, 4) + 1);
            builder.appendCodePoint(this.getCodePoint(random, size));
            remaining -= size;
        }

        return builder.toString();
    }

    private int getCodePoint(final RandomGenerator random, final int utf8Size) {
        return switch (utf8Size) {
            case 1 -> random.nextInt(0x20, 0x7F);
            case 2 -> random.nextInt(0xA0, 0x800);
            case 3 -> random.nextInt(0x4E00, 0xA000);
            default -> random.nextInt(0x1F300, 0x1FAFF);
        };
    }

}
