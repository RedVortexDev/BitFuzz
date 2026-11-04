package red.vortx.bitfuzz.buffer;

/// Thrown when a [ByteWriter] writes past its [writable][ByteWriter#writable()] bytes.
public class WriteOverflowException extends RuntimeException {

    public WriteOverflowException(final String message) {
        super(message);
    }

}
