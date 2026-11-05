package red.vortx.bitfuzz.schema.std;

import red.vortx.bitfuzz.buffer.ByteReader;
import red.vortx.bitfuzz.buffer.ByteWriter;
import red.vortx.bitfuzz.schema.api.SchemaException;
import red.vortx.bitfuzz.schema.api.field.type.CompositeType;
import red.vortx.bitfuzz.schema.api.field.type.Type;

import java.util.List;
import java.util.Objects;

/// A UTF-8 string. Prefixed with its length in bytes.
///
/// Writing a string with an unpaired surrogate, or reading malformed UTF-8, throws [SchemaException].
///
/// @param lengthType the type of the length prefix
public record StringType(Type<Integer> lengthType) implements CompositeType<String> {

    public StringType {
        Objects.requireNonNull(lengthType, "lengthType");
    }

    @Override
    public void write(final ByteWriter output, final String value) {
        final byte[] bytes = Utf8.encode(value);
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
        return Utf8.decode(bytes);
    }

    @Override
    public Class<String> javaType() {
        return String.class;
    }

    @Override
    public int sizeOf(final String value) {
        final int length = Utf8.encode(value).length;
        return this.lengthType.sizeOf(length) + length;
    }

    @Override
    public List<Part> parts(final String value) {
        final int length = Utf8.encode(value).length;

        return List.of(
                new Part("length", this.lengthType, length),
                new Part("", new FixedStringType(length), value)
        );
    }

}
