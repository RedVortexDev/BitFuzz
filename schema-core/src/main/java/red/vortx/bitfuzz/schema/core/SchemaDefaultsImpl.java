// This file is part of BitFuzz.
// SPDX-FileCopyrightText: 2026 RedVortexDev
// SPDX-License-Identifier: Apache-2.0

package red.vortx.bitfuzz.schema.core;

import red.vortx.bitfuzz.schema.api.SchemaDefaults;
import red.vortx.bitfuzz.schema.api.key.SchemaKey;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public record SchemaDefaultsImpl(Map<SchemaKey<?>, Object> values) implements SchemaDefaults {

    public SchemaDefaultsImpl {
        values = Map.copyOf(values);
    }

    @Override
    public <T> T get(final SchemaKey<T> key) {
        return getValue(this.values, key);
    }

    @Override
    public Mutable toMutable() {
        return new MutableImpl(this.values);
    }

    /// {@return a new empty mutable schema defaults}
    static SchemaDefaults.Mutable empty() {
        return new MutableImpl(Map.of());
    }

    private static <T> T getValue(final Map<SchemaKey<?>, Object> values, final SchemaKey<T> key) {
        Objects.requireNonNull(key, "key");

        final Object value = values.get(key);
        if (value == null) {
            return key.fallback();
        }

        //noinspection unchecked
        return (T) value;
    }

    private static class MutableImpl implements SchemaDefaults.Mutable {

        private final Map<SchemaKey<?>, Object> values;

        public MutableImpl(final Map<SchemaKey<?>, Object> values) {
            this.values = new HashMap<>(values);
        }

        @Override
        public <T> T get(final SchemaKey<T> key) {
            return getValue(this.values, key);
        }

        @Override
        public Mutable toMutable() {
            return new MutableImpl(this.values);
        }

        @Override
        public <T> Mutable set(final SchemaKey<T> key, final T value) {
            Objects.requireNonNull(key, "key");
            Objects.requireNonNull(value, "value");

            this.values.put(key, value);
            return this;
        }

        @Override
        public SchemaDefaults build() {
            return new SchemaDefaultsImpl(this.values);
        }

    }

}
