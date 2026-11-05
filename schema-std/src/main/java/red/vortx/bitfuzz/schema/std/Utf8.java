package red.vortx.bitfuzz.schema.std;

import red.vortx.bitfuzz.schema.api.SchemaException;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;

enum Utf8 {
    ;

    static byte[] encode(final String value) {
        try {
            final ByteBuffer encoded = StandardCharsets.UTF_8.newEncoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .encode(CharBuffer.wrap(value));
            final byte[] bytes = new byte[encoded.remaining()];
            encoded.get(bytes);
            return bytes;
        } catch (final CharacterCodingException e) {
            throw new SchemaException("String is not valid UTF-16 and cannot be encoded as UTF-8", e);
        }
    }

    static String decode(final byte[] bytes) {
        try {
            return StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(bytes))
                    .toString();
        } catch (final CharacterCodingException e) {
            throw new SchemaException("String bytes are not valid UTF-8", e);
        }
    }

}
