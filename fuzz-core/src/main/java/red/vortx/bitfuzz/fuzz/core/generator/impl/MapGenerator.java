package red.vortx.bitfuzz.fuzz.core.generator.impl;

import red.vortx.bitfuzz.fuzz.api.Generator;
import red.vortx.bitfuzz.fuzz.core.generator.Sizes;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.random.RandomGenerator;

/// Generates maps with unique keys.
///
/// With few possible keys (like booleans), maps can come out smaller than the picked size.
///
/// @param key   the generator for the keys
/// @param value the generator for the values
/// @param <K>   the type of the keys
/// @param <V>   the type of the values
public record MapGenerator<K, V>(Generator<K> key, Generator<V> value) implements Generator<Map<K, V>> {

    private static final int ATTEMPTS_PER_ENTRY = 10;

    public MapGenerator {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(value, "value");
    }

    @Override
    public Map<K, V> generate(final RandomGenerator random) {
        final int targetSize = Sizes.collection(random);
        final int maxAttempts = targetSize * ATTEMPTS_PER_ENTRY;
        final Map<K, V> map = LinkedHashMap.newLinkedHashMap(targetSize);

        for (int attempt = 0; attempt < maxAttempts && map.size() < targetSize; attempt++) {
            map.computeIfAbsent(this.key.generate(random), _ -> this.value.generate(random));
        }

        return map;
    }

}
