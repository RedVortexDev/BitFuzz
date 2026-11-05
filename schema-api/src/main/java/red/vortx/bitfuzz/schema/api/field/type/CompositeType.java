package red.vortx.bitfuzz.schema.api.field.type;

import java.util.List;

/// A composite type is a type serialized as multiple parts.
///
/// This is documentation-only, used for printing the structure of types to assist in debugging the serialized byte form.
///
/// @param <T> the type of the value
public interface CompositeType<T> extends Type<T> {

    /// {@return the structural parts of the value}
    ///
    /// @param value the value to get the parts of
    List<Part> parts(T value);

    /// A structural part of a composite type.
    ///
    /// @param label the label of the part
    /// @param type  the type of the part
    /// @param value the value of the part
    record Part(String label, Type<?> type, Object value) {

    }

}
