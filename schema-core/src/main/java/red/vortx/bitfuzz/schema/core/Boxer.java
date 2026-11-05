package red.vortx.bitfuzz.schema.core;

import java.lang.invoke.MethodType;

/// Utility class for boxing primitive types to their wrapper classes.
public enum Boxer {
    ;

    /// Box the given primitive type to its wrapper class.
    ///
    /// @param type the primitive type to box
    /// @return the corresponding wrapper class
    public static Class<?> box(final Class<?> type) {
        return MethodType.methodType(type).wrap().returnType();
    }

}
