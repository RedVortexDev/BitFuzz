// This file is part of BitFuzz.
// SPDX-FileCopyrightText: 2026 RedVortexDev
// SPDX-License-Identifier: Apache-2.0

package red.vortx.bitfuzz.schema.api.annotation;

import red.vortx.bitfuzz.schema.api.field.type.TypeSpec;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.Supplier;

/// Makes an annotation choose the type of whatever it annotates in a mapped record.
///
/// Built-in annotations are made the same way as user-defined ones:
/// ```java
/// @Retention(RUNTIME)
/// @Target(TYPE_USE)
/// @WireType(VarInt.Spec.class)
/// public @interface VarInt {
///     final class Spec implements Supplier<TypeSpec<?>> {
///         public TypeSpec<?> get() { return Types.VAR_INT; }
///     }
/// }
///
/// record Player(@VarInt int id, List<@VarInt Integer> scores) {}
/// ```
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface WireType {

    /// {@return the class that supplies the type}
    ///
    /// It needs a no-argument constructor.
    Class<? extends Supplier<? extends TypeSpec<?>>> value();

}
