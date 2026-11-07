// This file is part of BitFuzz.
// SPDX-FileCopyrightText: 2026 RedVortexDev
// SPDX-License-Identifier: Apache-2.0

package red.vortx.bitfuzz.schema.api.field.type;

import red.vortx.bitfuzz.buffer.ByteWriter;

import java.util.List;

/// A composite type is a type serialized as multiple parts.
///
/// This is documentation-only, used for printing the structure of types to assist in debugging the serialized byte form.
/// The parts must add up to exactly what [#write(ByteWriter, Object)] writes.
///
/// @param <T> the type of the value
public interface CompositeType<T> extends Type<T> {

    /// {@return the structural parts of the value}
    ///
    /// @param value the value to get the parts of
    List<Part> parts(T value);

    /// A structural part of a composite type.
    ///
    /// @param label the label of the part, or empty to reuse the parent's
    /// @param type  the type of the part
    /// @param value the value of the part
    record Part(String label, Type<?> type, Object value) {

    }

}
