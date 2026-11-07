// This file is part of BitFuzz.
// SPDX-FileCopyrightText: 2026 RedVortexDev
// SPDX-License-Identifier: Apache-2.0

package red.vortx.bitfuzz.fuzz.core.generator;

import org.junit.jupiter.api.Test;
import red.vortx.bitfuzz.fuzz.core.generator.impl.ListGenerator;
import red.vortx.bitfuzz.fuzz.core.generator.impl.MapGenerator;
import red.vortx.bitfuzz.fuzz.core.generator.impl.OptionalGenerator;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static red.vortx.bitfuzz.fuzz.core.generator.Samples.sample;

class ContainerGeneratorTest {

    @Test
    void listSizesStaySmallAndIncludeEmpty() {
        final List<List<Integer>> lists = sample(new ListGenerator<>(SingleGenerators.INT));

        assertThat(lists).anyMatch(List::isEmpty);
        assertThat(lists).anyMatch(list -> list.size() == Sizes.MAX_COLLECTION_SIZE);
        assertThat(lists).allMatch(list -> list.size() <= Sizes.MAX_COLLECTION_SIZE);
    }

    @Test
    void nestedListsAreBounded() {
        final List<List<List<Integer>>> lists = sample(new ListGenerator<>(new ListGenerator<>(SingleGenerators.INT)));

        assertThat(lists).allMatch(outer -> outer.stream().mapToInt(List::size).sum()
                <= Sizes.MAX_COLLECTION_SIZE * Sizes.MAX_COLLECTION_SIZE);
        assertThat(lists).anyMatch(outer -> outer.stream().anyMatch(inner -> !inner.isEmpty()));
    }

    @Test
    void mapsKeepInsertionOrder() {
        final List<Map<Integer, Long>> maps = sample(new MapGenerator<>(SingleGenerators.INT, SingleGenerators.LONG));

        assertThat(maps).allMatch(map -> map instanceof LinkedHashMap);
    }

    @Test
    void mapsReachTargetSizes() {
        final List<Map<Integer, Long>> maps = sample(new MapGenerator<>(SingleGenerators.INT, SingleGenerators.LONG));

        assertThat(maps).anyMatch(Map::isEmpty);
        assertThat(maps).anyMatch(map -> map.size() == Sizes.MAX_COLLECTION_SIZE);
    }

    @Test
    void mapsWithFewPossibleKeysTerminate() {
        final List<Map<Boolean, Integer>> maps = sample(new MapGenerator<>(SingleGenerators.BOOLEAN, SingleGenerators.INT));

        assertThat(maps).allMatch(map -> map.size() <= 2);
        assertThat(maps).anyMatch(map -> map.size() == 2);
    }

    @Test
    void optionalsAreBothEmptyAndPresent() {
        final List<Optional<Integer>> optionals = sample(new OptionalGenerator<>(SingleGenerators.INT));

        assertThat(optionals).anyMatch(Optional::isEmpty);
        assertThat(optionals).anyMatch(Optional::isPresent);
    }

}
