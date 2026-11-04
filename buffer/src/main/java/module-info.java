import org.jspecify.annotations.NullMarked;

@NullMarked
module red.vortx.bitfuzz.buffer {
    exports red.vortx.bitfuzz.buffer;
    exports red.vortx.bitfuzz.buffer.impl;
    requires static transitive org.jspecify;
}
