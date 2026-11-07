// This file is part of BitFuzz.
// SPDX-FileCopyrightText: 2026 RedVortexDev
// SPDX-License-Identifier: Apache-2.0

package red.vortx.bitfuzz.schema.api;

import java.util.ServiceLoader;

enum SchemaProviders {
    ;

    static final SchemaProvider PROVIDER = ServiceLoader.load(SchemaProvider.class, SchemaProvider.class.getClassLoader())
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No %s implementation found, is schema-core on the runtime path?"
                    .formatted(SchemaProvider.class.getName())
            ));

}
