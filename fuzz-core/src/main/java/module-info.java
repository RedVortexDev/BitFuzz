import org.jspecify.annotations.NullMarked;

@NullMarked
module red.vortx.bitfuzz.fuzz.core {
    requires static transitive org.jspecify;
    requires transitive red.vortx.bitfuzz.fuzz.api;
    requires red.vortx.bitfuzz.schema.std;
}
