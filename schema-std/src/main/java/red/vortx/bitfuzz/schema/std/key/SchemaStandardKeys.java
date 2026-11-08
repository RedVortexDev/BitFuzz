// This file is part of BitFuzz.
// SPDX-FileCopyrightText: 2026 RedVortexDev
// SPDX-License-Identifier: Apache-2.0

package red.vortx.bitfuzz.schema.std.key;

import red.vortx.bitfuzz.schema.api.SchemaDefaults;
import red.vortx.bitfuzz.schema.api.field.type.TypeSpec;
import red.vortx.bitfuzz.schema.api.key.SchemaKey;
import red.vortx.bitfuzz.schema.std.type.Types;
import red.vortx.bitfuzz.schema.std.type.enumeration.EnumEncoding;
import red.vortx.bitfuzz.schema.std.type.record.RecordTypes;

import java.util.function.UnaryOperator;

/// Holds [keys][SchemaKey] for the standard library schema options.
public enum SchemaStandardKeys {
    ;

    /// The type to use for length fields in variable-length types.
    /// Defaults to [Types#VAR_INT].
    ///
    /// This type can be addressed more concisely with [Types#LENGTH].
    public static final SchemaKey<TypeSpec<Integer>> LENGTH_TYPE = new SchemaKey<>("length type", Types.VAR_INT);

    /// The type to use for tag fields, e.g., the ordinal of an enum.
    /// Defaults to [Types#VAR_INT].
    ///
    /// This type can be addressed more concisely with [Types#TAG].
    public static final SchemaKey<TypeSpec<Integer>> TAG_TYPE = new SchemaKey<>("tag type", Types.VAR_INT);

    /// The way enum values should be encoded.
    /// Defaults to [EnumEncoding#ORDINAL].
    ///
    public static final SchemaKey<EnumEncoding> ENUM_ENCODING = new SchemaKey<>("enum encoding", EnumEncoding.ORDINAL);

    /// The types mapped records use for Java types instead of the built-in ones.
    ///
    /// Use [SchemaDefaults.Mutable#update(SchemaKey, UnaryOperator)] to modify this value rather than replacing it.
    ///
    /// @see RecordTypes#with(Class, TypeSpec)
    public static final SchemaKey<RecordTypes> RECORD_TYPES = new SchemaKey<>("record types", RecordTypes.empty());

}
