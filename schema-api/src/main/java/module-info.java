import org.jspecify.annotations.NullMarked;

@NullMarked
module red.vortx.bitfuzz.schema.api {
    requires static transitive org.jspecify;
    requires transitive red.vortx.bitfuzz.buffer;

    exports red.vortx.bitfuzz.schema.api;
    exports red.vortx.bitfuzz.schema.api.annotation;
    exports red.vortx.bitfuzz.schema.api.field;
    exports red.vortx.bitfuzz.schema.api.field.type;
    exports red.vortx.bitfuzz.schema.api.key;

    uses red.vortx.bitfuzz.schema.api.SchemaProvider;
}
