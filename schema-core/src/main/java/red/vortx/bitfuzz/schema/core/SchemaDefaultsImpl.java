package red.vortx.bitfuzz.schema.core;

import red.vortx.bitfuzz.schema.api.Endianness;
import red.vortx.bitfuzz.schema.api.SchemaDefaults;
import red.vortx.bitfuzz.schema.api.field.type.EndiannessTypeSpec;
import red.vortx.bitfuzz.schema.api.field.type.Type;
import red.vortx.bitfuzz.schema.api.field.type.TypeSpec;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public record SchemaDefaultsImpl(Endianness endianness, Type<Integer> lengthType,
                                 Map<Class<?>, TypeSpec<?>> recordTypes) implements SchemaDefaults {

    public SchemaDefaultsImpl {
        Objects.requireNonNull(endianness, "endianness");
        Objects.requireNonNull(lengthType, "lengthType");
        recordTypes = Map.copyOf(recordTypes);
    }

    public SchemaDefaultsImpl(final Endianness endianness, final Type<Integer> lengthType) {
        this(endianness, lengthType, Map.of());
    }

    @Override
    public <T> Optional<TypeSpec<T>> recordType(final Class<T> javaType) {
        return findRecordType(this.recordTypes, javaType);
    }

    public Mutable toMutable() {
        return new MutableImpl(this.endianness, this.lengthType, this.recordTypes);
    }

    /// {@return the defaults every new schema builder starts from: big-endian, with an endianness-aware int length}
    static SchemaDefaults.Mutable initial() {
        return new MutableImpl(
                Endianness.BIG_ENDIAN,
                new EndiannessTypeSpec<>(Primitives.INT, Primitives.INT_LE),
                Map.of()
        );
    }

    private static <T> Optional<TypeSpec<T>> findRecordType(final Map<Class<?>, TypeSpec<?>> recordTypes, final Class<T> javaType) {
        Objects.requireNonNull(javaType, "javaType");

        //noinspection unchecked
        return Optional.ofNullable((TypeSpec<T>) recordTypes.get(Boxer.box(javaType)));
    }

    private static class MutableImpl implements SchemaDefaults.Mutable {

        private Endianness endianness;
        private TypeSpec<Integer> lengthType;
        private final Map<Class<?>, TypeSpec<?>> recordTypes;

        public MutableImpl(final Endianness endianness, final TypeSpec<Integer> lengthType, final Map<Class<?>, TypeSpec<?>> recordTypes) {
            this.endianness = Objects.requireNonNull(endianness, "endianness");
            this.lengthType = Objects.requireNonNull(lengthType, "lengthType");
            this.recordTypes = new HashMap<>(recordTypes);
        }

        @Override
        public Endianness endianness() {
            return this.endianness;
        }

        @Override
        public Type<Integer> lengthType() {
            return this.lengthType.resolve(this);
        }

        @Override
        public <T> Optional<TypeSpec<T>> recordType(final Class<T> javaType) {
            return findRecordType(this.recordTypes, javaType);
        }

        @Override
        public Mutable toMutable() {
            return new MutableImpl(this.endianness, this.lengthType, this.recordTypes);
        }

        @Override
        public Mutable endianness(final Endianness endianness) {
            this.endianness = Objects.requireNonNull(endianness, "endianness");
            return this;
        }

        @Override
        public Mutable lengthType(final TypeSpec<Integer> lengthType) {
            this.lengthType = Objects.requireNonNull(lengthType, "lengthType");
            return this;
        }

        @Override
        public <T> Mutable recordType(final Class<T> javaType, final TypeSpec<T> type) {
            Objects.requireNonNull(javaType, "javaType");
            Objects.requireNonNull(type, "type");

            this.recordTypes.put(Boxer.box(javaType), type);
            return this;
        }

        @Override
        public SchemaDefaults build() {
            return new SchemaDefaultsImpl(this.endianness, this.lengthType.resolve(this), this.recordTypes);
        }

    }

}
