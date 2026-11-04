package red.vortx.bitfuzz.schema.core;

import red.vortx.bitfuzz.schema.api.SchemaBuilder;
import red.vortx.bitfuzz.schema.api.SchemaProvider;

import java.util.ServiceLoader;

/// The schema-core [SchemaProvider], registered for [ServiceLoader].
public final class SchemaProviderImpl implements SchemaProvider {

    @Override
    public SchemaBuilder builder() {
        return new SchemaBuilderImpl();
    }

}
