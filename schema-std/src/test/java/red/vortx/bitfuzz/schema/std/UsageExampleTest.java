// This file is part of BitFuzz.
// SPDX-FileCopyrightText: 2026 RedVortexDev
// SPDX-License-Identifier: Apache-2.0

package red.vortx.bitfuzz.schema.std;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import red.vortx.bitfuzz.buffer.netty.NettyByteReader;
import red.vortx.bitfuzz.buffer.netty.NettyByteWriter;
import red.vortx.bitfuzz.fuzz.api.FuzzFailure;
import red.vortx.bitfuzz.fuzz.api.FuzzResult;
import red.vortx.bitfuzz.fuzz.core.Fuzz;
import red.vortx.bitfuzz.schema.api.Endianness;
import red.vortx.bitfuzz.schema.api.Schema;
import red.vortx.bitfuzz.schema.api.SchemaBuilder;
import red.vortx.bitfuzz.schema.api.SchemaException;
import red.vortx.bitfuzz.schema.api.field.type.SimpleType;
import red.vortx.bitfuzz.schema.api.field.type.Type;
import red.vortx.bitfuzz.schema.api.key.SchemaKeys;
import red.vortx.bitfuzz.schema.std.type.Types;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/// BitFuzz the way a user would use it. First as a plain codec, then fuzzing that same codec.
class UsageExampleTest {

    /// A chat message in a made-up network protocol.
    static final Schema CHAT_MESSAGE = SchemaBuilder.create()
            .field(Types.VAR_INT, "messageId")
            .field(Types.UUID, "sender")
            .field(Types.STRING, "text")
            .field(Types.list(Types.STRING), "mentions")
            .field(Types.optional(Types.VAR_INT), "replyTo")
            .build();

    static final UUID SENDER = UUID.fromString("6f1c2a3b-4d5e-4f60-8a9b-0c1d2e3f4a5b");

    static final List<Object> MESSAGE = List.of(
            42,
            SENDER,
            "hello @alex and @sam",
            List.of("alex", "sam"),
            Optional.of(41)
    );

    private static byte[] send(final Schema schema, final List<Object> values) {
        final ByteBuf outgoing = Unpooled.buffer();
        schema.write(new NettyByteWriter(outgoing), values);

        return ByteBufUtil.getBytes(outgoing);
    }

    private static List<Object> receive(final Schema schema, final byte[] wire) {
        return schema.read(new NettyByteReader(Unpooled.wrappedBuffer(wire)));
    }

    @Nested
    class NormalUsage {

        @Test
        void sendAndReceiveAMessage() {
            final byte[] wire = send(CHAT_MESSAGE, MESSAGE);
            final List<Object> received = receive(CHAT_MESSAGE, wire);

            assertThat(received.get(0)).isEqualTo(42);
            assertThat(received.get(1)).isEqualTo(SENDER);
            assertThat(received.get(2)).isEqualTo("hello @alex and @sam");
            assertThat(received.get(3)).isEqualTo(List.of("alex", "sam"));
            assertThat(received.get(4)).isEqualTo(Optional.of(41));
            assertThat(wire)
                    .as("varints and varint lengths: 1 + 16 + (1 + 20) + (1 + 5 + 4) + (1 + 1) bytes")
                    .hasSize(50);
        }

        @Test
        void describeALittleEndianFileHeader() {
            final Schema header = SchemaBuilder.create()
                    .defaults(defaults -> defaults.set(SchemaKeys.ENDIANNESS, Endianness.LITTLE_ENDIAN))
                    .field(Types.INT, "magic")
                    .field(Types.SHORT, "version")
                    .field(Types.LONG, "createdAt")
                    .build();
            final List<Object> values = List.of(0x46_5A_54_42, (short) 2, 1_700_000_000_000L);

            final byte[] wire = send(header, values);

            assertThat(Arrays.copyOf(wire, 4)).as("magic, lowest byte first").containsExactly(0x42, 0x54, 0x5A, 0x46);
            assertThat(receive(header, wire)).containsExactlyElementsOf(values);
        }

        @Test
        void rejectBadInputSafely() {
            final byte[] wire = send(CHAT_MESSAGE, MESSAGE);
            final byte[] cutOffInText = Arrays.copyOf(wire, 20);

            assertThatThrownBy(() -> receive(CHAT_MESSAGE, cutOffInText))
                    .isInstanceOf(SchemaException.class)
                    .hasMessageContaining("text");
        }

        @Test
        void rejectWrongValuesBeforeWritingAnything() {
            final ByteBuf buffer = Unpooled.buffer();
            final List<Object> idIsNotANumber = List.of("not an id", SENDER, "", List.of(), Optional.empty());

            assertThatThrownBy(() -> CHAT_MESSAGE.write(new NettyByteWriter(buffer), idIsNotANumber))
                    .isInstanceOf(SchemaException.class)
                    .hasMessageContaining("messageId");
            assertThat(buffer.readableBytes()).isZero();
        }

    }

    @Nested
    @Timeout(120)
    class Fuzzing {

        @Test
        void fuzzTheChatProtocol() {
            final FuzzResult roundTrip = Fuzz.roundTrip(CHAT_MESSAGE, 10_000, 42);
            final FuzzResult mutated = Fuzz.mutate(CHAT_MESSAGE, 50_000, 42);

            System.out.printf("[chat round trip] %s%n", roundTrip.report());
            System.out.printf("[chat mutation]   %s%n", mutated.report());

            assertThat(roundTrip.isClean()).as(roundTrip.report()).isTrue();
            assertThat(mutated.isClean()).as(mutated.report()).isTrue();
        }

        /// A user writes their own string type for a legacy format and forgets to validate the length prefix.
        /// Everyday use and valid values work fine, but damaged bytes crash it, and the fuzzer shrinks the crash to the
        /// 5 bytes that cause it.
        @Test
        void fuzzerCatchesABugInACustomType() {
            final Type<String> legacyString = new SimpleType<>(String.class,
                    (out, value) -> {
                        final byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
                        out.writeInt(bytes.length);
                        out.writeBytes(bytes);
                    },
                    in -> {
                        final byte[] bytes = new byte[in.readInt()];
                        in.readBytes(bytes);
                        return new String(bytes, StandardCharsets.UTF_8);
                    }
            );
            final Schema legacy = SchemaBuilder.create()
                    .field(Types.VAR_INT, "id")
                    .field(legacyString, "name")
                    .build();

            assertThat(receive(legacy, send(legacy, List.of(1, "steve")))).as("everyday use").containsExactly(1, "steve");
            assertThat(Fuzz.roundTrip(legacy, 5_000, 42).isClean()).as("valid values").isTrue();

            final FuzzResult result = Fuzz.mutate(legacy, 20_000, 42);
            System.out.printf("[legacy mutation] %s%n", result.report());

            final FuzzFailure crash = result.failures().stream()
                    .filter(failure -> failure.failure() instanceof NegativeArraySizeException)
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("Expected the missing length check to be found%n%s"
                            .formatted(result.report())
                    ));

            assertThat(crash.shrunkInput()).as("a varint id, then a negative length").hasSize(5);
            assertThat(crash.shrunkInput()[1]).isNegative();
        }

    }

}
