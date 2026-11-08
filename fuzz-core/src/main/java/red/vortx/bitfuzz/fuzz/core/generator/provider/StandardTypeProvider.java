// This file is part of BitFuzz.
// SPDX-FileCopyrightText: 2026 RedVortexDev
// SPDX-License-Identifier: Apache-2.0

package red.vortx.bitfuzz.fuzz.core.generator.provider;

import red.vortx.bitfuzz.fuzz.api.Generator;
import red.vortx.bitfuzz.fuzz.api.GeneratorLookup;
import red.vortx.bitfuzz.fuzz.api.GeneratorProvider;
import red.vortx.bitfuzz.fuzz.core.generator.impl.EnumGenerator;
import red.vortx.bitfuzz.fuzz.core.generator.impl.FixedStringGenerator;
import red.vortx.bitfuzz.fuzz.core.generator.impl.ListGenerator;
import red.vortx.bitfuzz.fuzz.core.generator.impl.MapGenerator;
import red.vortx.bitfuzz.fuzz.core.generator.impl.OptionalGenerator;
import red.vortx.bitfuzz.schema.api.field.type.Type;
import red.vortx.bitfuzz.schema.std.type.BooleanType;
import red.vortx.bitfuzz.schema.std.type.FixedStringType;
import red.vortx.bitfuzz.schema.std.type.ListType;
import red.vortx.bitfuzz.schema.std.type.MapType;
import red.vortx.bitfuzz.schema.std.type.OptionalType;
import red.vortx.bitfuzz.schema.std.type.StandardType;
import red.vortx.bitfuzz.schema.std.type.StringType;
import red.vortx.bitfuzz.schema.std.type.VarIntType;
import red.vortx.bitfuzz.schema.std.type.VarLongType;
import red.vortx.bitfuzz.schema.std.type.enumeration.EnumType;
import red.vortx.bitfuzz.schema.std.type.record.RecordType;

import java.util.Optional;

/// Provides generators for `schema-std` types that need more than their [Java type][Type#javaType()].
public record StandardTypeProvider() implements GeneratorProvider {

    @Override
    public <T> Optional<Generator<T>> create(final Type<T> type, final GeneratorLookup lookup) {
        if (!(type instanceof final StandardType<T> standardType)) {
            return Optional.empty();
        }

        final Generator<?> generator = switch (standardType) {
            case FixedStringType(final int byteLength) -> new FixedStringGenerator(byteLength);
            case final EnumType<?> enumType -> new EnumGenerator<>(enumType.enumClass().getEnumConstants());
            case ListType<?>(final Type<?> element, _) -> new ListGenerator<>(lookup.ofType(element));
            case MapType<?, ?>(final Type<?> key, final Type<?> value, _) ->
                    new MapGenerator<>(lookup.ofType(key), lookup.ofType(value));
            case OptionalType<?>(final Type<?> value) -> new OptionalGenerator<>(lookup.ofType(value));
            // JavaTypeProvider & RecordProvider covers these.
            case BooleanType _, VarIntType _, VarLongType _, StringType _, RecordType<?> _ -> null;
        };

        //noinspection unchecked
        return Optional.ofNullable((Generator<T>) generator);
    }

}
