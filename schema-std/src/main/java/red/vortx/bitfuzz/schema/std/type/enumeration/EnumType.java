// This file is part of BitFuzz.
// SPDX-FileCopyrightText: 2026 RedVortexDev
// SPDX-License-Identifier: Apache-2.0

package red.vortx.bitfuzz.schema.std.type.enumeration;

import red.vortx.bitfuzz.schema.std.type.StandardType;

/// An enum constant, written as its ordinal or its name.
///
/// @param <E> the enum type
public sealed interface EnumType<E extends Enum<E>> extends StandardType<E> permits OrdinalEnumType, NamedEnumType {

    /// {@return the enum class}
    Class<E> enumClass();

    @Override
    default Class<E> javaType() {
        return this.enumClass();
    }

}
