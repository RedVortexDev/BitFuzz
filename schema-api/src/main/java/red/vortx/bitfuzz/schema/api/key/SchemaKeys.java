package red.vortx.bitfuzz.schema.api.key;

import red.vortx.bitfuzz.schema.api.Endianness;

/// Holds [keys][SchemaKey] for the base library schema options.
public enum SchemaKeys {
    ;

    /// The default endianness to use in schema operations.
    public static final SchemaKey<Endianness> ENDIANNESS = new SchemaKey<>("endianness", Endianness.BIG_ENDIAN);

}
