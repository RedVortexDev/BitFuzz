// This file is part of BitFuzz.
// SPDX-FileCopyrightText: 2026 RedVortexDev
// SPDX-License-Identifier: Apache-2.0

package red.vortx.bitfuzz.schema.std;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import red.vortx.bitfuzz.buffer.netty.NettyByteReader;
import red.vortx.bitfuzz.buffer.netty.NettyByteWriter;
import red.vortx.bitfuzz.fuzz.api.FuzzResult;
import red.vortx.bitfuzz.fuzz.core.Fuzz;
import red.vortx.bitfuzz.schema.api.Schema;
import red.vortx.bitfuzz.schema.api.SchemaBuilder;
import red.vortx.bitfuzz.schema.std.type.Types;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SimpleUsageTest {

    static final Schema PLAYER = SchemaBuilder.create()
            .field(Types.STRING, "name")
            .field(Types.INT, "level")
            .field(Types.BOOL, "online")
            .build();

    @Test
    void writeAndReadAPlayer() {
        final ByteBuf buffer = Unpooled.buffer();
        final List<Object> player = List.of("steve", 12, true);

        PLAYER.write(new NettyByteWriter(buffer), player);
        final List<Object> read = PLAYER.read(new NettyByteReader(buffer));

        assertThat(read).isEqualTo(player);
    }

    @Test
    @Timeout(60)
    void fuzzThePlayerSchema() {
        final FuzzResult roundTrip = Fuzz.roundTrip(PLAYER, 1_000, 42);
        final FuzzResult mutated = Fuzz.mutate(PLAYER, 10_000, 42);

        assertThat(roundTrip.isClean()).as(roundTrip.report()).isTrue();
        assertThat(mutated.isClean()).as(mutated.report()).isTrue();
    }

    // --

    record Player(
            String name, int level, boolean online
    ) {

    }

    static final Schema PLAYER_RECORD = SchemaBuilder.create()
            .field(Types.record(Player.class), "player")
            .build();

    @Test
    void writeAndReadAPlayerRecord() {
        final ByteBuf buffer = Unpooled.buffer();
        final List<Object> player = List.of(new Player(
                "steve", 12, true
        ));

        PLAYER_RECORD.write(new NettyByteWriter(buffer), player);

        final List<Object> read = PLAYER_RECORD.read(new NettyByteReader(buffer));

        assertThat(read).isEqualTo(player);
    }

}
