package red.vortx.bitfuzz.schema.api.field;

import red.vortx.bitfuzz.schema.api.field.type.Type;

import java.util.Objects;

/// A field in a schema.
///
/// @param type the type of the field
/// @param name the name of the field
public record Field(Type<?> type, String name) {

    public Field {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(name, "name");
    }

}
