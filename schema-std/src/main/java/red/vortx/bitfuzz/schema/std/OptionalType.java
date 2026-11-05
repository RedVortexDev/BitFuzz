package red.vortx.bitfuzz.schema.std;

import red.vortx.bitfuzz.buffer.ByteReader;
import red.vortx.bitfuzz.buffer.ByteWriter;
import red.vortx.bitfuzz.schema.api.SchemaException;
import red.vortx.bitfuzz.schema.api.field.type.Type;

import java.util.Objects;
import java.util.Optional;

/// An optional value. A presence byte (`0` empty, `1` present) followed by the value when present.
/// Any other presence byte is rejected.
///
/// @param valueType the type of the value
/// @param <T>       the type of the value
public record OptionalType<T>(Type<T> valueType) implements Type<Optional<T>> {

    private static final byte EMPTY = 0;
    private static final byte PRESENT = 1;

    public OptionalType {
        Objects.requireNonNull(valueType, "valueType");
    }

    @Override
    public void write(final ByteWriter output, final Optional<T> value) {
        if (value.isPresent()) {
            output.writeByte(PRESENT);
            this.valueType.write(output, value.get());
        } else {
            output.writeByte(EMPTY);
        }
    }

    @Override
    public Optional<T> read(final ByteReader input) {
        final byte presence = input.readByte();
        return switch (presence) {
            case EMPTY -> Optional.empty();
            case PRESENT -> Optional.of(this.valueType.read(input));
            default -> throw new SchemaException("Invalid optional presence value: %d".formatted(presence));
        };
    }

    @Override
    public Class<Optional<T>> javaType() {
        //noinspection unchecked
        return (Class<Optional<T>>) (Class<?>) Optional.class;
    }

}
