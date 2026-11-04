import org.jspecify.annotations.NullMarked;

@NullMarked
module red.vortx.bitfuzz.buffer.netty {
    requires static transitive org.jspecify;
    requires transitive red.vortx.bitfuzz.buffer;
    requires transitive io.netty.buffer;

    exports red.vortx.bitfuzz.buffer.netty;
}
