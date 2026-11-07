// This file is part of BitFuzz.
// SPDX-FileCopyrightText: 2026 RedVortexDev
// SPDX-License-Identifier: Apache-2.0

package red.vortx.bitfuzz.fuzz.api;

import red.vortx.bitfuzz.schema.api.field.type.Type;

/// Finds a [Generator] for a type.
public interface GeneratorLookup {

    /// Gets a generator for the given type.
    ///
    /// @param type the type to generate values for
    /// @param <T>  the type of the values
    /// @return a generator for the type
    /// @throws IllegalArgumentException if no generator supports the type
    <T> Generator<T> ofType(Type<T> type);

}
