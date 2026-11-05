package red.vortx.bitfuzz.schema.std.record;

import red.vortx.bitfuzz.buffer.ByteReader;
import red.vortx.bitfuzz.buffer.ByteWriter;
import red.vortx.bitfuzz.schema.api.SchemaException;
import red.vortx.bitfuzz.schema.api.field.type.CompositeType;
import red.vortx.bitfuzz.schema.api.field.type.Type;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/// A record. Each component in declaration order, with nothing in between.
///
/// Reading calls the record's canonical constructor, so its validation also rejects decoded values.
///
/// @param javaType    the record class
/// @param components  the components in declaration order
/// @param constructor the canonical constructor
/// @param <R>         the record type
public record RecordType<R extends Record>(Class<R> javaType, List<Component> components,
                                           Constructor<R> constructor) implements CompositeType<R> {

    public RecordType {
        Objects.requireNonNull(javaType, "javaType");
        Objects.requireNonNull(constructor, "constructor");
        components = List.copyOf(components);
    }

    @Override
    public void write(final ByteWriter output, final R value) {
        for (final Component component : this.components) {
            final Object componentValue = this.getComponentValue(component, value);

            //noinspection ConstantValue -- API trust issues
            if (componentValue == null) {
                throw new SchemaException("Component '%s' of %s is null"
                        .formatted(component.name(), this.javaType.getSimpleName())
                );
            }

            component.type().writeUnchecked(output, componentValue);
        }
    }

    @Override
    public R read(final ByteReader input) {
        final Object[] values = new Object[this.components.size()];
        for (int i = 0; i < values.length; i++) {
            values[i] = this.components.get(i).type().read(input);
        }

        return this.construct(values);
    }

    /// Creates a record from component values in declaration order.
    ///
    /// @param values the component values
    /// @return the record
    /// @throws SchemaException if the record's constructor rejects the values
    public R construct(final Object... values) {
        try {
            return this.constructor.newInstance(values);
        } catch (final InvocationTargetException e) {
            throw new SchemaException("%s rejected the values: %s"
                    .formatted(this.javaType.getSimpleName(), e.getCause()), e.getCause()
            );
        } catch (final ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot construct %s".formatted(this.javaType.getName()), e);
        }
    }

    @Override
    public List<Part> parts(final R value) {
        final List<Part> parts = new ArrayList<>(this.components.size());
        for (final Component component : this.components) {
            parts.add(new Part(component.name(), component.type(), this.getComponentValue(component, value)));
        }

        return parts;
    }

    private Object getComponentValue(final Component component, final R value) {
        try {
            return component.accessor().invoke(value);
        } catch (final InvocationTargetException e) {
            throw new IllegalStateException("Accessor of '%s' threw".formatted(component.name()), e.getCause());
        } catch (final IllegalAccessException e) {
            throw new IllegalStateException("Cannot access component '%s'".formatted(component.name()), e);
        }
    }

    /// A record component.
    ///
    /// @param name     the component name
    /// @param accessor the accessor method
    /// @param type     the type of the component
    public record Component(String name, Method accessor, Type<?> type) {

        public Component {
            Objects.requireNonNull(name, "name");
            Objects.requireNonNull(accessor, "accessor");
            Objects.requireNonNull(type, "type");
        }

    }

}
