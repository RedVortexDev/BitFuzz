// This file is part of BitFuzz.
// SPDX-FileCopyrightText: 2026 RedVortexDev
// SPDX-License-Identifier: Apache-2.0

package red.vortx.bitfuzz.schema.api.field.type;

import red.vortx.bitfuzz.schema.api.SchemaDefaults;

/// A specification for a type that can be resolved into a [Type] when a schema is built.
///
/// @param <T> the type of the value
public interface TypeSpec<T> {

    /// Resolves this type specification into a [Type] using the given schema defaults.
    ///
    /// @param defaults the schema defaults to use
    /// @return the resolved type
    Type<T> resolve(SchemaDefaults defaults);

}
