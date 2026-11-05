package red.vortx.bitfuzz.schema.core;

import red.vortx.bitfuzz.schema.api.Schema;
import red.vortx.bitfuzz.schema.api.SchemaBuilder;
import red.vortx.bitfuzz.schema.api.SchemaDefaults;
import red.vortx.bitfuzz.schema.api.field.Field;
import red.vortx.bitfuzz.schema.api.field.type.TypeSpec;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class SchemaBuilderImpl implements SchemaBuilder {

    private final SchemaDefaults.Mutable defaults = SchemaDefaultsImpl.initial();
    private final List<BuilderField> fields = new ArrayList<>();

    @Override
    public SchemaBuilder field(final TypeSpec<?> type, final String name) {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(name, "name");

        this.fields.add(new BuilderField(type, name));
        return this;
    }

    @Override
    public SchemaBuilder defaults(final Consumer<SchemaDefaults.Mutable> defaults) {
        Objects.requireNonNull(defaults, "defaults");

        defaults.accept(this.defaults);
        return this;
    }

    @Override
    public Schema build() {
        final SchemaDefaults builtDefaults = this.defaults.build();

        final List<Field> builtFields = this.fields.stream()
                .map(field -> field.build(builtDefaults))
                .toList();

        return new SchemaImpl(builtFields, builtDefaults);
    }

    record BuilderField(TypeSpec<?> type, String name) {

        public Field build(final SchemaDefaults defaults) {
            return new Field(this.type.resolve(defaults), this.name);
        }

    }

}
