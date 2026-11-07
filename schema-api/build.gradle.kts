// This file is part of BitFuzz.
// SPDX-FileCopyrightText: 2026 RedVortexDev
// SPDX-License-Identifier: Apache-2.0

plugins {
    id("bitfuzz.java-library")
}

mavenPublishing {
    pom {
        name = "BitFuzz Schema API"
        description = "API for defining binary protocol schemas."
    }
}

dependencies {
    api(project(":buffer"))
}
