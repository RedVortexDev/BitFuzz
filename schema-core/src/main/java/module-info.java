import org.jspecify.annotations.NullMarked;

@NullMarked
module red.vortx.bitfuzz.schema.core {
    exports red.vortx.bitfuzz.schema.core;
    requires static transitive org.jspecify;
    requires transitive red.vortx.bitfuzz.schema.api;

    provides red.vortx.bitfuzz.schema.api.SchemaProvider with red.vortx.bitfuzz.schema.core.SchemaProviderImpl;
}
