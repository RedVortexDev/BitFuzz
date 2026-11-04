package red.vortx.bitfuzz.buffer;

/// Thrown when a [ByteReader] reads past its [remaining][ByteReader#remaining()] bytes.
public class ReadUnderflowException extends RuntimeException {

    public ReadUnderflowException(final String message) {
        super(message);
    }

}
