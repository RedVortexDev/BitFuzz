import org.jspecify.annotations.NullMarked;

@NullMarked
module red.vortx.bitfuzz.fuzz.api {
    requires static transitive org.jspecify;
    requires transitive red.vortx.bitfuzz.schema.api;
}
