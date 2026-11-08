// This file is part of BitFuzz.
// SPDX-FileCopyrightText: 2026 RedVortexDev
// SPDX-License-Identifier: Apache-2.0

package red.vortx.bitfuzz.schema.std.type;

import red.vortx.bitfuzz.schema.api.field.type.Type;
import red.vortx.bitfuzz.schema.std.type.enumeration.EnumType;
import red.vortx.bitfuzz.schema.std.type.record.RecordType;

/// Marks a type as a part of the standard library.
///
/// @param <T> the type of the values
public sealed interface StandardType<T> extends Type<T> permits
        BooleanType, VarIntType, VarLongType, StringType, FixedStringType,
        ListType, MapType, OptionalType, EnumType, RecordType {

}
