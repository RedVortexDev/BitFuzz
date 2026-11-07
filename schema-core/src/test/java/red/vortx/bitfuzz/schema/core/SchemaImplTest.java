// This file is part of BitFuzz.
// SPDX-FileCopyrightText: 2026 RedVortexDev
// SPDX-License-Identifier: Apache-2.0

package red.vortx.bitfuzz.schema.core;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;
import red.vortx.bitfuzz.buffer.ByteReader;
import red.vortx.bitfuzz.buffer.ReadUnderflowException;
import red.vortx.bitfuzz.buffer.netty.NettyByteWriter;
import red.vortx.bitfuzz.schema.api.Schema;
import red.vortx.bitfuzz.schema.api.SchemaException;
import red.vortx.bitfuzz.schema.api.field.Field;
import red.vortx.bitfuzz.schema.api.field.type.Primitives;
import red.vortx.bitfuzz.schema.api.field.type.SimpleType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static red.vortx.bitfuzz.schema.core.TestBuffers.reader;
import static red.vortx.bitfuzz.schema.core.TestBuffers.written;

class SchemaImplTest {

    private static Schema intLongDouble() {
        return new SchemaBuilderImpl()
                .field(Primitives.INT, "int")
                .field(Primitives.LONG, "long")
                .field(Primitives.DOUBLE, "double")
                .build();
    }

    @Test
    void roundTrip() {
        final Schema schema = intLongDouble();
        final List<Object> values = List.of(1, 2L, 3.0);

        final byte[] bytes = written(out -> schema.write(out, values));

        assertThat(bytes).hasSize(4 + 8 + 8);
        assertThat(schema.read(reader(bytes))).containsExactlyElementsOf(values);
    }

    @Test
    void writesFieldsInOrder() {
        final Schema schema = new SchemaBuilderImpl()
                .field(Primitives.BYTE, "a")
                .field(Primitives.SHORT, "b")
                .build();

        final byte[] bytes = written(out -> schema.write(out, List.of((byte) 1, (short) 0x0203)));

        assertThat(bytes).containsExactly(1, 2, 3);
    }

    @Test
    void emptySchema() {
        final Schema schema = new SchemaBuilderImpl().build();

        assertThat(written(out -> schema.write(out, List.of()))).isEmpty();
        assertThat(schema.read(reader(new byte[0]))).isEmpty();
    }

    @Test
    void readLeavesTrailingBytesUnread() {
        final Schema schema = new SchemaBuilderImpl().field(Primitives.BYTE, "a").build();
        final ByteReader input = reader(1, 2, 3);

        assertThat(schema.read(input)).containsExactly((byte) 1);
        assertThat(input.remaining()).isEqualTo(2);
    }

    @Test
    void tooFewValues() {
        final Schema schema = intLongDouble();

        assertThatThrownBy(() -> written(out -> schema.write(out, List.of(1, 2L))))
                .isInstanceOf(SchemaException.class)
                .hasMessageContaining("expected: 3")
                .hasMessageContaining("got: 2");
    }

    @Test
    void tooManyValues() {
        final Schema schema = intLongDouble();

        assertThatThrownBy(() -> written(out -> schema.write(out, List.of(1, 2L, 3.0, 4))))
                .isInstanceOf(SchemaException.class)
                .hasMessageContaining("expected: 3")
                .hasMessageContaining("got: 4");
    }

    @Test
    void wrongValueType() {
        final Schema schema = intLongDouble();

        assertThatThrownBy(() -> written(out -> schema.write(out, List.of(1, 2, 3.0))))
                .isInstanceOf(SchemaException.class)
                .hasMessageContaining("long")
                .hasMessageContaining("Long")
                .hasMessageContaining("Integer");
    }

    @Test
    void wrongValueTypeWritesNothing() {
        final Schema schema = intLongDouble();
        final ByteBuf buffer = Unpooled.buffer();

        assertThatThrownBy(() -> schema.write(new NettyByteWriter(buffer), List.of("nope", 2L, 3.0)))
                .isInstanceOf(SchemaException.class);
        assertThat(buffer.readableBytes()).isZero();
    }

    @Test
    void nullValue() {
        final Schema schema = intLongDouble();
        final List<Object> values = Arrays.asList(1, null, 3.0);

        assertThatThrownBy(() -> written(out -> schema.write(out, values)))
                .isInstanceOf(SchemaException.class)
                .hasMessageContaining("index 1");
    }

    @Test
    void deliberateWriteRejectionIsWrapped() {
        final SchemaException rejection = new SchemaException("no");
        final Schema schema = new SchemaBuilderImpl()
                .field(new SimpleType<>(Integer.class, (out, value) -> {
                    throw rejection;
                }, in -> 0), "picky")
                .build();

        assertThatThrownBy(() -> written(out -> schema.write(out, List.of(1))))
                .isInstanceOf(SchemaException.class)
                .hasMessageContaining("picky")
                .hasCause(rejection);
    }

    @Test
    void unexpectedWriteFailurePropagatesUnchanged() {
        final IllegalStateException bug = new IllegalStateException("boom");
        final Schema schema = new SchemaBuilderImpl()
                .field(new SimpleType<>(Integer.class, (out, value) -> {
                    throw bug;
                }, in -> 0), "broken")
                .build();

        assertThatThrownBy(() -> written(out -> schema.write(out, List.of(1)))).isSameAs(bug);
    }

    @Test
    void unexpectedReadFailurePropagatesUnchanged() {
        final IllegalStateException bug = new IllegalStateException("boom");
        final Schema schema = new SchemaBuilderImpl()
                .field(new SimpleType<>(Integer.class, (out, value) -> {
                }, in -> {
                    throw bug;
                }), "broken")
                .build();

        assertThatThrownBy(() -> schema.read(reader(new byte[0]))).isSameAs(bug);
    }

    @Test
    void deliberateRejectionMayCarryAnyCause() {
        final Schema schema = new SchemaBuilderImpl()
                .field(new SimpleType<>(Integer.class, (out, value) -> {
                }, in -> {
                    throw new SchemaException("bad data", new IllegalStateException("detail"));
                }), "picky")
                .build();

        assertThatThrownBy(() -> schema.read(reader(new byte[0])))
                .isInstanceOf(SchemaException.class)
                .hasMessageContaining("picky");
    }

    @Test
    void readUnderflowIsWrapped() {
        final Schema schema = intLongDouble();

        assertThatThrownBy(() -> schema.read(reader(0, 0, 0, 1, 0, 0)))
                .isInstanceOf(SchemaException.class)
                .hasMessageContaining("long")
                .hasCauseInstanceOf(ReadUnderflowException.class);
    }

    @Test
    void fieldsAreUnmodifiable() {
        final Schema schema = intLongDouble();

        assertThatThrownBy(() -> schema.fields().add(new Field(Primitives.INT, "extra")))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void fieldsAreCopied() {
        final List<Field> source = new ArrayList<>(List.of(new Field(Primitives.INT, "a")));
        final SchemaImpl schema = new SchemaImpl(source, ((SchemaImpl) new SchemaBuilderImpl().build()).defaults());

        source.add(new Field(Primitives.INT, "b"));

        assertThat(schema.fields()).hasSize(1);
    }

}
