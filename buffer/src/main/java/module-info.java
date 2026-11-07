// This file is part of BitFuzz.
// SPDX-FileCopyrightText: 2026 RedVortexDev
// SPDX-License-Identifier: Apache-2.0

import org.jspecify.annotations.NullMarked;

@NullMarked
module red.vortx.bitfuzz.buffer {
    exports red.vortx.bitfuzz.buffer;
    exports red.vortx.bitfuzz.buffer.impl;
    requires static transitive org.jspecify;
}
