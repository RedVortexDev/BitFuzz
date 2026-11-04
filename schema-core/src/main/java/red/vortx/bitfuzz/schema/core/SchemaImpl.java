package red.vortx.bitfuzz.schema.core;

import red.vortx.bitfuzz.buffer.ByteReader;
import red.vortx.bitfuzz.buffer.ByteWriter;
import red.vortx.bitfuzz.buffer.ReadUnderflowException;
import red.vortx.bitfuzz.schema.api.Schema;
import red.vortx.bitfuzz.schema.api.SchemaDefaults;
import red.vortx.bitfuzz.schema.api.SchemaException;
import red.vortx.bitfuzz.schema.api.field.Field;

import java.util.ArrayList;
import java.util.List;

public record SchemaImpl(List<Field> fields, SchemaDefaults defaults) implements Schema {

    public SchemaImpl(final List<Field> fields, final SchemaDefaults defaults) {
        this.fields = List.copyOf(fields);
        this.defaults = defaults;
    }

    @Override
    public void write(final ByteWriter output, final List<Object> values) throws SchemaException {
        if (values.size() != this.fields.size()) {
            throw new SchemaException("Wrong number of values, expected: %d, got: %d"
                    .formatted(this.fields.size(), values.size())
            );
        }

        for (int i = 0; i < this.fields.size(); i++) {
            final Field field = this.fields.get(i);
            final Object value = values.get(i);

            //noinspection ConstantValue -- API trust issues
            if (value == null) {
                throw new SchemaException("Value is null at index %d".formatted(i));
            }

            if (!field.type().javaType().isInstance(value)) {
                throw new SchemaException("Value for field '%s' is not of the correct type, expected: %s, got: %s"
                        .formatted(field.name(), field.type().javaType().getSimpleName(), value.getClass().getSimpleName())
                );
            }

            try {
                field.type().writeUnchecked(output, value);
            } catch (final Exception e) {
                if (this.isIntendedException(e) || e instanceof ClassCastException) {
                    throw new SchemaException("Failed to write value for field '%s' at index %d"
                            .formatted(field.name(), i), e
                    );
                }
                throw e;
            }
        }
    }

    @Override
    public List<Object> read(final ByteReader input) {
        final List<Object> values = new ArrayList<>(this.fields.size());

        for (final Field field : this.fields) {
            try {
                final Object value = field.type().read(input);
                values.add(value);
            } catch (final Exception e) {
                if (this.isIntendedException(e)) {
                    throw new SchemaException("Failed to read value for field '%s'".formatted(field.name()), e);
                }
                throw e;
            }
        }

        return values;
    }

    @Override
    public List<Field> fields() {
        return this.fields;
    }

    boolean isIntendedException(final Throwable throwable) {
        return throwable instanceof SchemaException || throwable instanceof ReadUnderflowException;
    }

}
