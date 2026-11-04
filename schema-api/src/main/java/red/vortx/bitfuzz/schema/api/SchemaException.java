package red.vortx.bitfuzz.schema.api;

/// Thrown when values or bytes don't fit a schema.
public class SchemaException extends RuntimeException {

    public SchemaException(final String message) {
        super(message);
    }

    public SchemaException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
