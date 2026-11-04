import org.jspecify.annotations.NullMarked;

@NullMarked
module red.vortx.bitfuzz.schema.std {
    exports red.vortx.bitfuzz.schema.std;

    requires static transitive org.jspecify;
    requires transitive red.vortx.bitfuzz.schema.core;
}
