package red.vortx.bitfuzz.schema.std.type.record;

import red.vortx.bitfuzz.schema.api.annotation.WireType;
import red.vortx.bitfuzz.schema.api.field.type.TypeSpec;
import red.vortx.bitfuzz.schema.std.type.Types;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.Supplier;

/// Encodes an `int` or [Integer] as a [varint][Types#VAR_INT].
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE_USE)
@WireType(VarInt.Spec.class)
public @interface VarInt {

    final class Spec implements Supplier<TypeSpec<?>> {

        @Override
        public TypeSpec<?> get() {
            return Types.VAR_INT;
        }

    }

}
