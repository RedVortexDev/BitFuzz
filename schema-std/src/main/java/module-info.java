// This file is part of BitFuzz.
// SPDX-FileCopyrightText: 2026 RedVortexDev
// SPDX-License-Identifier: Apache-2.0

import org.jspecify.annotations.NullMarked;

@NullMarked
module red.vortx.bitfuzz.schema.std {
    exports red.vortx.bitfuzz.schema.std.key;
    exports red.vortx.bitfuzz.schema.std.type;
    exports red.vortx.bitfuzz.schema.std.type.record;
    exports red.vortx.bitfuzz.schema.std.type.enumeration;

    requires static transitive org.jspecify;
    requires transitive red.vortx.bitfuzz.schema.core;
}
