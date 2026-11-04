import org.jspecify.annotations.NullMarked;

@NullMarked
module red.vortx.bitfuzz.fuzz.core {
    requires static transitive org.jspecify;
    requires transitive red.vortx.bitfuzz.fuzz.api;
    requires red.vortx.bitfuzz.schema.std;
    requires red.vortx.bitfuzz.buffer.nio;

    exports red.vortx.bitfuzz.fuzz.core;
    exports red.vortx.bitfuzz.fuzz.core.generator;
    exports red.vortx.bitfuzz.fuzz.core.generator.provider;
    exports red.vortx.bitfuzz.fuzz.core.generator.impl;
    exports red.vortx.bitfuzz.fuzz.core.mutator;
    exports red.vortx.bitfuzz.fuzz.core.shrink;
    exports red.vortx.bitfuzz.fuzz.core.runner;
}
