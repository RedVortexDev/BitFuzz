package red.vortx.bitfuzz.schema.std.type.record;

import red.vortx.bitfuzz.schema.api.field.type.TypeSpec;
import red.vortx.bitfuzz.schema.core.Boxer;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/// The types mapped records use for Java types instead of the built-in ones.
///
/// Primitive classes and their wrappers are the same key, so `int.class` also covers [Integer].
///
/// @param types the type for each Java type
public record RecordTypes(Map<Class<?>, TypeSpec<?>> types) {

    private static final RecordTypes EMPTY = new RecordTypes(Map.of());

    public RecordTypes {
        final Map<Class<?>, TypeSpec<?>> boxed = new HashMap<>(types.size());
        for (final Map.Entry<Class<?>, TypeSpec<?>> entry : types.entrySet()) {
            Objects.requireNonNull(entry.getValue(), "type");

            final Class<?> javaType = Boxer.box(entry.getKey());
            if (boxed.put(javaType, entry.getValue()) != null) {
                throw new IllegalArgumentException("Record type for %s is set twice".formatted(javaType.getSimpleName()));
            }
        }

        types = Map.copyOf(boxed);
    }

    /// {@return record types with nothing replaced}
    public static RecordTypes empty() {
        return EMPTY;
    }

    /// {@return a copy that also uses `type` for `javaType`, replacing any type set for it before}
    ///
    /// @param javaType the Java type
    /// @param type     the type to use
    /// @param <T>      the Java type
    public <T> RecordTypes with(final Class<T> javaType, final TypeSpec<T> type) {
        Objects.requireNonNull(javaType, "javaType");
        Objects.requireNonNull(type, "type");

        final Map<Class<?>, TypeSpec<?>> combined = new HashMap<>(this.types);
        combined.put(Boxer.box(javaType), type);

        return new RecordTypes(combined);
    }

    /// Gets the type used for a Java type instead of the built-in one.
    ///
    /// @param javaType the Java type
    /// @param <T>      the Java type
    /// @return the type, or empty if none was set
    public <T> Optional<TypeSpec<T>> get(final Class<T> javaType) {
        Objects.requireNonNull(javaType, "javaType");

        //noinspection unchecked
        return Optional.ofNullable((TypeSpec<T>) this.types.get(Boxer.box(javaType)));
    }

}
