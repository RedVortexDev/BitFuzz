// This file is part of BitFuzz.
// SPDX-FileCopyrightText: 2026 RedVortexDev
// SPDX-License-Identifier: Apache-2.0

package red.vortx.bitfuzz.schema.std.type.enumeration;

import red.vortx.bitfuzz.buffer.ByteReader;
import red.vortx.bitfuzz.buffer.ByteWriter;
import red.vortx.bitfuzz.schema.api.SchemaException;
import red.vortx.bitfuzz.schema.api.field.type.Type;

import java.util.Objects;

/// An enum constant encoded as its ordinal.
///
/// @param enumClass the enum class
/// @param tagType   the type of the ordinal tag
/// @param <E>       the enum type
public record OrdinalEnumType<E extends Enum<E>>(Class<E> enumClass, Type<Integer> tagType) implements EnumType<E> {

    public OrdinalEnumType {
        Objects.requireNonNull(enumClass, "enumClass");
        Objects.requireNonNull(tagType, "tagType");
    }

    @Override
    public void write(final ByteWriter output, final E value) {
        this.tagType.write(output, value.ordinal());
    }

    @Override
    public E read(final ByteReader input) {
        final int ordinal = this.tagType.read(input);
        final E[] constants = this.enumClass.getEnumConstants();

        if (ordinal < 0 || ordinal >= constants.length) {
            throw new SchemaException("Invalid ordinal %d for enum %s".formatted(ordinal, this.enumClass.getName()));
        }

        return constants[ordinal];
    }

}
