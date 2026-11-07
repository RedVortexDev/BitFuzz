// This file is part of BitFuzz.
// SPDX-FileCopyrightText: 2026 RedVortexDev
// SPDX-License-Identifier: Apache-2.0

package red.vortx.bitfuzz.schema.api;

import java.util.ServiceLoader;

/// Creates schema builders for [SchemaBuilder#create()].
///
/// Found with [ServiceLoader]. `schema-core` provides one.
public interface SchemaProvider {

    /// {@return a new, empty schema builder}
    SchemaBuilder builder();

}
