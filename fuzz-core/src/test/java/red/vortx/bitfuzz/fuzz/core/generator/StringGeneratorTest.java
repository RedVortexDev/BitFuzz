package red.vortx.bitfuzz.fuzz.core.generator;

import org.junit.jupiter.api.Test;
import red.vortx.bitfuzz.fuzz.core.generator.impl.StringGenerator;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static red.vortx.bitfuzz.fuzz.core.generator.Samples.sample;

class StringGeneratorTest {

    private static final List<String> VALUES = sample(new StringGenerator());

    private static boolean hasUnpairedSurrogate(final String value) {
        for (int i = 0; i < value.length(); i++) {
            final char current = value.charAt(i);
            if (Character.isHighSurrogate(current)) {
                if (i + 1 >= value.length() || !Character.isLowSurrogate(value.charAt(i + 1))) {
                    return true;
                }
                i++;
            } else if (Character.isLowSurrogate(current)) {
                return true;
            }
        }
        return false;
    }

    @Test
    void neverProducesUnpairedSurrogates() {
        assertThat(VALUES).noneMatch(StringGeneratorTest::hasUnpairedSurrogate);
    }

    @Test
    void includesEmptyAndEdgeStrings() {
        assertThat(VALUES).contains("", "\0", "😀");
    }

    @Test
    void coversEveryUtf8Width() {
        assertThat(VALUES).anyMatch(value -> value.codePoints().anyMatch(cp -> cp < 0x80));
        assertThat(VALUES).anyMatch(value -> value.codePoints().anyMatch(cp -> cp >= 0x80 && cp < 0x800));
        assertThat(VALUES).anyMatch(value -> value.codePoints().anyMatch(cp -> cp >= 0x800 && cp < 0x10000));
        assertThat(VALUES).anyMatch(value -> value.codePoints().anyMatch(cp -> cp >= 0x10000));
    }

    @Test
    void mostlyShortSometimesLong() {
        final long shortStrings = VALUES.stream().filter(value -> value.codePointCount(0, value.length()) <= 16).count();

        assertThat(shortStrings).isGreaterThan(VALUES.size() / 2);
        assertThat(VALUES).anyMatch(value -> value.getBytes(StandardCharsets.UTF_8).length > 1000);
    }

}
