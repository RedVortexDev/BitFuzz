package red.vortx.bitfuzz.schema.api.field;

import red.vortx.bitfuzz.schema.api.field.type.Type;

/// A field in a schema.
///
/// @param type the type of the field
/// @param name the name of the field
public record Field(Type<?> type, String name) {

}
