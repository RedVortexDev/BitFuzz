// This file is part of BitFuzz.
// SPDX-FileCopyrightText: 2026 RedVortexDev
// SPDX-License-Identifier: Apache-2.0

plugins {
    id("bitfuzz.java-library")
}

mavenPublishing {
    pom {
        name = "BitFuzz Schema Core"
        description = "Implementation of the BitFuzz schema API."
    }
}

dependencies {
    api(project(":schema-api"))

    testImplementation(project(":buffer-netty"))
}
