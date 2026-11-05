package red.vortx.bitfuzz.schema.std;

import red.vortx.bitfuzz.buffer.ByteReader;
import red.vortx.bitfuzz.buffer.ByteWriter;
import red.vortx.bitfuzz.schema.api.SchemaException;
import red.vortx.bitfuzz.schema.api.field.type.Type;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/// A UTF-8 string. Prefixed with its length in bytes.
///
/// Strict encoding and decoding, paired surrogates when writing and malformed UTF-8 when reading throw
/// [SchemaException].
///
/// @param lengthType the type of the length prefix
public record StringType(Type<Integer> lengthType) implements Type<String> {

    public StringType {
        Objects.requireNonNull(lengthType, "lengthType");
    }

    @Override
    public void write(final ByteWriter output, final String value) {
        final byte[] bytes = encode(value);
        this.lengthType.write(output, bytes.length);
        output.writeBytes(bytes);
    }

    @Override
    public String read(final ByteReader input) {
        final int length = this.lengthType.read(input);

        if (length < 0) {
            throw new SchemaException("String length is negative: %d".formatted(length));
        }
        if (length > input.remaining()) {
            throw new SchemaException("String length %d is greater than the %d readable bytes"
                    .formatted(length, input.remaining())
            );
        }

        final byte[] bytes = new byte[length];
        input.readBytes(bytes);
        return decode(bytes);
    }

    @Override
    public Class<String> javaType() {
        return String.class;
    }

    @Override
    public int sizeOf(final String value) {
        final int length = encode(value).length;
        return this.lengthType.sizeOf(length) + length;
    }

    private static byte[] encode(final String value) {
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

    private static String decode(final byte[] bytes) {
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
