// This file is part of BitFuzz.
// SPDX-FileCopyrightText: 2026 RedVortexDev
// SPDX-License-Identifier: Apache-2.0

package red.vortx.bitfuzz.schema.api;

import org.junit.jupiter.api.Test;
import red.vortx.bitfuzz.buffer.ByteReader;
import red.vortx.bitfuzz.buffer.ByteWriter;
import red.vortx.bitfuzz.schema.api.field.Field;
import red.vortx.bitfuzz.schema.api.field.type.EndiannessTypeSpec;
import red.vortx.bitfuzz.schema.api.field.type.SimpleType;
import red.vortx.bitfuzz.schema.api.field.type.Type;

import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class NullSafetyTest {

    private static final Type<Integer> INT = new SimpleType<>(Integer.class, ByteWriter::writeInt, ByteReader::readInt);

    @Test
    void fieldRejectsNulls() {
        assertThatNullPointerException().isThrownBy(() -> new Field(null, "name")).withMessage("type");
        assertThatNullPointerException().isThrownBy(() -> new Field(INT, null)).withMessage("name");
    }

    @Test
    void simpleTypeRejectsNulls() {
        assertThatNullPointerException()
                .isThrownBy(() -> new SimpleType<>(null, ByteWriter::writeInt, ByteReader::readInt))
                .withMessage("javaType");
        assertThatNullPointerException()
                .isThrownBy(() -> new SimpleType<Integer>(Integer.class, null, ByteReader::readInt))
                .withMessage("writer");
        assertThatNullPointerException()
                .isThrownBy(() -> new SimpleType<Integer>(Integer.class, ByteWriter::writeInt, null))
                .withMessage("reader");
    }

    @Test
    void endiannessSpecRejectsNulls() {
        assertThatNullPointerException()
                .isThrownBy(() -> new EndiannessTypeSpec<>(null, INT))
                .withMessage("bigEndianType");
        assertThatNullPointerException()
                .isThrownBy(() -> new EndiannessTypeSpec<>(INT, null))
                .withMessage("littleEndianType");
    }

}
