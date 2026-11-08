// This file is part of BitFuzz.
// SPDX-FileCopyrightText: 2026 RedVortexDev
// SPDX-License-Identifier: Apache-2.0

package red.vortx.bitfuzz.schema.std.type.enumeration;

import red.vortx.bitfuzz.schema.std.key.SchemaStandardKeys;
import red.vortx.bitfuzz.schema.std.type.Types;

/// The way enum values should be encoded.
///
/// @see SchemaStandardKeys#ENUM_ENCODING
public enum EnumEncoding {

    /// The ordinal, as the schema's [tag type][Types#TAG].
    ORDINAL,
    /// The name, as a [string][Types#STRING].
    NAME

}
