// This file is part of BitFuzz.
// SPDX-FileCopyrightText: 2026 RedVortexDev
// SPDX-License-Identifier: Apache-2.0

package red.vortx.bitfuzz.schema.std.type.record;

import red.vortx.bitfuzz.schema.api.annotation.WireType;
import red.vortx.bitfuzz.schema.api.field.type.TypeSpec;
import red.vortx.bitfuzz.schema.std.type.Types;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.Supplier;

/// Encodes a `long` or [Long] as a [varlong][Types#VAR_LONG].
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE_USE)
@WireType(VarLong.Spec.class)
public @interface VarLong {

    final class Spec implements Supplier<TypeSpec<?>> {

        @Override
        public TypeSpec<?> get() {
            return Types.VAR_LONG;
        }

    }

}
