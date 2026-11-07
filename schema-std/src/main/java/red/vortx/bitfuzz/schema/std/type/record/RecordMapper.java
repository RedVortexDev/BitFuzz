// This file is part of BitFuzz.
// SPDX-FileCopyrightText: 2026 RedVortexDev
// SPDX-License-Identifier: Apache-2.0

package red.vortx.bitfuzz.schema.std.type.record;

import red.vortx.bitfuzz.schema.api.SchemaDefaults;
import red.vortx.bitfuzz.schema.api.annotation.WireType;
import red.vortx.bitfuzz.schema.api.field.type.Type;
import red.vortx.bitfuzz.schema.api.field.type.TypeSpec;
import red.vortx.bitfuzz.schema.core.Boxer;
import red.vortx.bitfuzz.schema.std.key.SchemaStandardKeys;
import red.vortx.bitfuzz.schema.std.type.ListType;
import red.vortx.bitfuzz.schema.std.type.MapType;
import red.vortx.bitfuzz.schema.std.type.OptionalType;
import red.vortx.bitfuzz.schema.std.type.Types;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.Supplier;

/// Maps records to [record types][RecordType] using reflection.
///
/// Each component's type is picked in this order:
/// 1. A [WireType] annotation on it
/// 2. The schema's [record type][RecordTypes] for its Java type
/// 3. The built-in type for numbers, `boolean`, `String`, `UUID`, records, `List`, `Optional` and `Map`
///
/// Building a schema with an unsupported record throws [IllegalArgumentException].
public enum RecordMapper {
    ;

    private static final Map<Class<?>, TypeSpec<?>> DEFAULTS = Map.ofEntries(
            Map.entry(Byte.class, Types.BYTE),
            Map.entry(Short.class, Types.SHORT),
            Map.entry(Integer.class, Types.INT),
            Map.entry(Long.class, Types.LONG),
            Map.entry(Float.class, Types.FLOAT),
            Map.entry(Double.class, Types.DOUBLE),
            Map.entry(Boolean.class, Types.BOOL),
            Map.entry(String.class, Types.STRING),
            Map.entry(UUID.class, Types.UUID)
    );

    /// Creates a spec that maps the given record when a schema is built.
    ///
    /// @param recordClass the record class
    /// @param <R>         the record type
    /// @return a spec for the record
    public static <R extends Record> TypeSpec<R> spec(final Class<R> recordClass) {
        Objects.requireNonNull(recordClass, "recordClass");

        return defaults -> new Mapping(defaults, new ArrayDeque<>()).map(recordClass);
    }

    private static Optional<TypeSpec<?>> findWireType(final AnnotatedType annotated) {
        for (final Annotation annotation : annotated.getAnnotations()) {
            final WireType wireType = annotation.annotationType().getAnnotation(WireType.class);
            if (wireType != null) {
                return Optional.of(instantiate(wireType.value()).get());
            }
        }

        return Optional.empty();
    }

    private static Class<?> getRawClass(final AnnotatedType annotated, final String location) {
        return switch (annotated.getType()) {
            case final Class<?> type -> type;
            case final ParameterizedType parameterized -> (Class<?>) parameterized.getRawType();
            default -> throw new IllegalArgumentException("Unsupported type %s in %s, use a concrete type"
                    .formatted(annotated.getType().getTypeName(), location)
            );
        };
    }

    private static <S extends Supplier<? extends TypeSpec<?>>> S instantiate(final Class<S> supplierClass) {
        try {
            final Constructor<S> constructor = supplierClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (final ReflectiveOperationException e) {
            throw new IllegalArgumentException("Cannot create wire type supplier %s".formatted(supplierClass.getName()), e);
        }
    }

    private static <R extends Record> Constructor<R> findCanonicalConstructor(final Class<R> recordClass, final Class<?>[] parameterTypes) {
        try {
            final Constructor<R> constructor = recordClass.getDeclaredConstructor(parameterTypes);
            constructor.setAccessible(true);
            return constructor;
        } catch (final NoSuchMethodException e) {
            throw new IllegalStateException("Record %s has no canonical constructor".formatted(recordClass.getName()), e);
        }
    }

    private record Mapping(SchemaDefaults defaults, Deque<Class<?>> resolving) {

        private <R extends Record> RecordType<R> map(final Class<R> recordClass) {
            if (this.resolving.contains(recordClass)) {
                throw new IllegalArgumentException("Recursive record %s is not supported"
                        .formatted(recordClass.getName())
                );
            }

            final RecordComponent[] recordComponents = recordClass.getRecordComponents();
            final List<RecordType.Component> components = new ArrayList<>(recordComponents.length);
            final Class<?>[] parameterTypes = new Class<?>[recordComponents.length];

            this.resolving.push(recordClass);
            for (int i = 0; i < recordComponents.length; i++) {
                components.add(this.mapComponent(recordClass, recordComponents[i]));
                parameterTypes[i] = recordComponents[i].getType();
            }
            this.resolving.pop();

            return new RecordType<>(recordClass, components, findCanonicalConstructor(recordClass, parameterTypes));
        }

        private RecordType.Component mapComponent(final Class<?> owner, final RecordComponent component) {
            final Method accessor = component.getAccessor();
            accessor.setAccessible(true);

            final String location = "component '%s' of %s".formatted(component.getName(), owner.getName());
            final Type<?> type = this.resolveType(component.getAnnotatedType(), location);

            return new RecordType.Component(component.getName(), accessor, type);
        }

        private Type<?> resolveType(final AnnotatedType annotated, final String location) {
            final Optional<TypeSpec<?>> annotatedSpec = findWireType(annotated);
            if (annotatedSpec.isPresent()) {
                return annotatedSpec.get().resolve(this.defaults);
            }

            final Class<?> raw = Boxer.box(getRawClass(annotated, location));
            final Optional<? extends TypeSpec<?>> schemaSpec = this.defaults.get(SchemaStandardKeys.RECORD_TYPES).get(raw);
            if (schemaSpec.isPresent()) {
                return schemaSpec.get().resolve(this.defaults);
            }

            final TypeSpec<?> defaultSpec = DEFAULTS.get(raw);
            if (defaultSpec != null) {
                return defaultSpec.resolve(this.defaults);
            }
            if (raw.isRecord()) {
                return this.map(raw.asSubclass(Record.class));
            }

            if (annotated instanceof final AnnotatedParameterizedType parameterized) {
                final AnnotatedType[] arguments = parameterized.getAnnotatedActualTypeArguments();
                if (raw == List.class) {
                    return new ListType<>(this.resolveType(arguments[0], location), Types.LENGTH.resolve(this.defaults));
                }
                if (raw == Optional.class) {
                    return new OptionalType<>(this.resolveType(arguments[0], location));
                }
                if (raw == Map.class) {
                    final Type<?> keyType = this.resolveType(arguments[0], location);
                    final Type<?> valueType = this.resolveType(arguments[1], location);
                    return new MapType<>(keyType, valueType, Types.LENGTH.resolve(this.defaults));
                }
            }

            throw new IllegalArgumentException("No wire type for %s in %s, annotate it with a @WireType annotation"
                    .formatted(annotated.getType().getTypeName(), location)
            );
        }

    }

}
