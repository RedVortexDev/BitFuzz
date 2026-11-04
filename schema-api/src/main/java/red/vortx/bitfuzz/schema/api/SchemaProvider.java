package red.vortx.bitfuzz.schema.api;

import java.util.ServiceLoader;

/// Creates schema builders for [SchemaBuilder#create()].
///
/// Found with [ServiceLoader]. `schema-core` provides one.
public interface SchemaProvider {

    /// {@return a new, empty schema builder}
    SchemaBuilder builder();

}
