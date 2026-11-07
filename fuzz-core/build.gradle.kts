// This file is part of BitFuzz.
// SPDX-FileCopyrightText: 2026 RedVortexDev
// SPDX-License-Identifier: Apache-2.0

plugins {
    id("bitfuzz.java-library")
}

mavenPublishing {
    pom {
        name = "BitFuzz Fuzz Core"
        description = "Standard generators, mutators, shrinker, and runners for fuzzing BitFuzz schemas."
    }
}

dependencies {
    api(project(":fuzz-api"))
    implementation(project(":schema-std"))
    implementation(project(":buffer-nio"))
}
