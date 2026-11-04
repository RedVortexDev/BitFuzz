import org.jspecify.annotations.NullMarked;

@NullMarked
module red.vortx.bitfuzz.buffer.nio {
    requires static transitive org.jspecify;
    requires transitive red.vortx.bitfuzz.buffer;

    exports red.vortx.bitfuzz.buffer.nio;
}
