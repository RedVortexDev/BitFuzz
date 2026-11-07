// This file is part of BitFuzz.
// SPDX-FileCopyrightText: 2026 RedVortexDev
// SPDX-License-Identifier: Apache-2.0

plugins {
    id("bitfuzz.java-library")
}

mavenPublishing {
    pom {
        name = "BitFuzz Buffer NIO"
        description = "BitFuzz byte reader and writer backed by a Java NIO ByteBuffer."
    }
}

dependencies {
    api(project(":buffer"))

    testImplementation(testFixtures(project(":buffer")))
}
