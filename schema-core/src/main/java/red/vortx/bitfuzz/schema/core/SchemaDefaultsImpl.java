package red.vortx.bitfuzz.schema.core;

import red.vortx.bitfuzz.schema.api.Endianness;
import red.vortx.bitfuzz.schema.api.SchemaDefaults;
import red.vortx.bitfuzz.schema.api.field.type.EndiannessTypeSpec;
import red.vortx.bitfuzz.schema.api.field.type.Type;
import red.vortx.bitfuzz.schema.api.field.type.TypeSpec;

public record SchemaDefaultsImpl(Endianness endianness, Type<Integer> lengthType) implements SchemaDefaults {

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
            this.endianness = endianness;
            this.lengthType = lengthType;
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
            return this;
        }

        @Override
        public Mutable endianness(final Endianness endianness) {
            this.endianness = endianness;
            return this;
        }

        @Override
        public Mutable lengthType(final TypeSpec<Integer> lengthType) {
            this.lengthType = lengthType;
            return this;
        }

        @Override
        public SchemaDefaults build() {
            return new SchemaDefaultsImpl(this.endianness, this.lengthType.resolve(this));
        }

    }

}
