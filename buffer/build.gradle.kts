// This file is part of BitFuzz.
// SPDX-FileCopyrightText: 2026 RedVortexDev
// SPDX-License-Identifier: Apache-2.0

plugins {
    id("bitfuzz.java-library")
    `java-test-fixtures`
}

mavenPublishing {
    pom {
        name = "BitFuzz Buffer"
        description = "Base interfaces for reading and writing bytes."
    }
}

dependencies {
    testFixturesApi(platform(libs.junit.bom))
    testFixturesApi(libs.junit.jupiter)
    testFixturesApi(libs.assertj.core)
}

afterEvaluate {
    val javaComponent = components["java"] as AdhocComponentWithVariants
    listOf("testFixturesApiElements", "testFixturesRuntimeElements", "testFixturesSourcesElements").forEach { name ->
        configurations.findByName(name)?.let { javaComponent.withVariantsFromConfiguration(it) { skip() } }
    }
}
