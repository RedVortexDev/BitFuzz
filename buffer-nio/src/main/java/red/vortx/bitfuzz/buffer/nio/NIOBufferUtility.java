package red.vortx.bitfuzz.buffer.nio;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

enum NIOBufferUtility {
    ;

    private static boolean isBigEndian(final ByteBuffer buffer) {
        return buffer.order() == ByteOrder.BIG_ENDIAN;
    }

    static short bigEndian(final ByteBuffer buffer, final short value) {
        return isBigEndian(buffer) ? value : Short.reverseBytes(value);
    }

    static int bigEndian(final ByteBuffer buffer, final int value) {
        return isBigEndian(buffer) ? value : Integer.reverseBytes(value);
    }

    static long bigEndian(final ByteBuffer buffer, final long value) {
        return isBigEndian(buffer) ? value : Long.reverseBytes(value);
    }

    static short littleEndian(final ByteBuffer buffer, final short value) {
        return isBigEndian(buffer) ? Short.reverseBytes(value) : value;
    }

    static int littleEndian(final ByteBuffer buffer, final int value) {
        return isBigEndian(buffer) ? Integer.reverseBytes(value) : value;
    }

    static long littleEndian(final ByteBuffer buffer, final long value) {
        return isBigEndian(buffer) ? Long.reverseBytes(value) : value;
    }

}
