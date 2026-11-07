// This file is part of BitFuzz.
// SPDX-FileCopyrightText: 2026 RedVortexDev
// SPDX-License-Identifier: Apache-2.0

plugins {
    id("bitfuzz.java-library")
}

dependencies {
    api(project(":fuzz-api"))
    implementation(project(":schema-std"))
    implementation(project(":buffer-nio"))
}
