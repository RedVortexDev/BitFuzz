// This file is part of BitFuzz.
// SPDX-FileCopyrightText: 2026 RedVortexDev
// SPDX-License-Identifier: Apache-2.0

plugins {
    id("bitfuzz.java-library")
}

mavenPublishing {
    pom {
        name = "BitFuzz Schema Std"
        description = "Standard library of BitFuzz schema types."
    }
}

dependencies {
    api(project(":schema-core"))

    testImplementation(project(":buffer-netty"))
    testImplementation(project(":fuzz-core"))
}
