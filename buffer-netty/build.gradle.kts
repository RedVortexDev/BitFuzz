// This file is part of BitFuzz.
// SPDX-FileCopyrightText: 2026 RedVortexDev
// SPDX-License-Identifier: Apache-2.0

plugins {
    id("bitfuzz.java-library")
}

mavenPublishing {
    pom {
        name = "BitFuzz Buffer Netty"
        description = "BitFuzz byte reader and writer backed by a Netty ByteBuf."
    }
}

dependencies {
    api(project(":buffer"))
    api(libs.netty.buffer)

    testImplementation(testFixtures(project(":buffer")))
}
