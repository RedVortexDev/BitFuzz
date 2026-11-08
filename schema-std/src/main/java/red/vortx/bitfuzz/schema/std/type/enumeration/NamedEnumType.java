// This file is part of BitFuzz.
// SPDX-FileCopyrightText: 2026 RedVortexDev
// SPDX-License-Identifier: Apache-2.0

package red.vortx.bitfuzz.schema.std.type.enumeration;

import red.vortx.bitfuzz.buffer.ByteReader;
import red.vortx.bitfuzz.buffer.ByteWriter;
import red.vortx.bitfuzz.schema.api.SchemaException;
import red.vortx.bitfuzz.schema.api.field.type.Type;

import java.util.Objects;

/// An enum constant encoded as its [name][Enum#name()].
///
/// @param enumClass the enum class
/// @param nameType  the type of the name
/// @param <E>       the enum type
public record NamedEnumType<E extends Enum<E>>(Class<E> enumClass, Type<String> nameType) implements EnumType<E> {

    public NamedEnumType {
        Objects.requireNonNull(enumClass, "enumClass");
        Objects.requireNonNull(nameType, "nameType");
    }

    @Override
    public void write(final ByteWriter output, final E value) {
        this.nameType.write(output, value.name());
    }

    @Override
    public E read(final ByteReader input) {
        final String name = this.nameType.read(input);

        try {
            return Enum.valueOf(this.enumClass, name);
        } catch (final IllegalArgumentException _) {
            throw new SchemaException("Invalid name '%s' for enum %s".formatted(name, this.enumClass.getName()));
        }
    }

}
