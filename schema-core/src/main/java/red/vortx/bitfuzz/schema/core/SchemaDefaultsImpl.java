package red.vortx.bitfuzz.schema.core;

import red.vortx.bitfuzz.schema.api.Endianness;
import red.vortx.bitfuzz.schema.api.SchemaDefaults;
import red.vortx.bitfuzz.schema.api.field.type.EndiannessTypeSpec;
import red.vortx.bitfuzz.schema.api.field.type.Type;
import red.vortx.bitfuzz.schema.api.field.type.TypeSpec;

import java.util.Objects;

public record SchemaDefaultsImpl(Endianness endianness, Type<Integer> lengthType) implements SchemaDefaults {

    public SchemaDefaultsImpl {
        Objects.requireNonNull(endianness, "endianness");
        Objects.requireNonNull(lengthType, "lengthType");
    }

    public Mutable toMutable() {
        return new MutableImpl(this.endianness, this.lengthType);
    }

    /// {@return the defaults every new schema builder starts from: big-endian, with an endianness-aware int length}
    static SchemaDefaults.Mutable initial() {
        return new MutableImpl(Endianness.BIG_ENDIAN, new EndiannessTypeSpec<>(Primitives.INT, Primitives.INT_LE));
    }

    private static class MutableImpl implements SchemaDefaults.Mutable {

        private Endianness endianness;
        private TypeSpec<Integer> lengthType;

        public MutableImpl(final Endianness endianness, final TypeSpec<Integer> lengthType) {
            this.endianness = Objects.requireNonNull(endianness, "endianness");
            this.lengthType = Objects.requireNonNull(lengthType, "lengthType");
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
        public Mutable toMutable() {
            return new MutableImpl(this.endianness, this.lengthType);
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
        public SchemaDefaults build() {
            return new SchemaDefaultsImpl(this.endianness, this.lengthType.resolve(this));
        }

    }

}
