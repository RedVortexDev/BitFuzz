// This file is part of BitFuzz.
// SPDX-FileCopyrightText: 2026 RedVortexDev
// SPDX-License-Identifier: Apache-2.0

import org.jspecify.annotations.NullMarked;

@NullMarked
module red.vortx.bitfuzz.fuzz.api {
    requires static transitive org.jspecify;
    requires transitive red.vortx.bitfuzz.schema.api;

    exports red.vortx.bitfuzz.fuzz.api;
}
