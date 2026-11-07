// This file is part of BitFuzz.
// SPDX-FileCopyrightText: 2026 RedVortexDev
// SPDX-License-Identifier: Apache-2.0

plugins {
    `java-library`
    id("com.vanniktech.maven.publish")
}

group = "red.vortx.bitfuzz"
version = "1.0.0"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

val libs = the<VersionCatalogsExtension>().named("libs")

dependencies {
    api(libs.findLibrary("jspecify").get())

    testImplementation(platform(libs.findLibrary("junit-bom").get()))
    testImplementation(libs.findLibrary("junit-jupiter").get())
    testImplementation(libs.findLibrary("assertj-core").get())
    testRuntimeOnly(libs.findLibrary("junit-platform-launcher").get())
}

tasks.test {
    useJUnitPlatform()
    maxHeapSize = "1g"
}

tasks.javadoc {
    (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:all,-missing", "-quiet")
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()

    pom {
        url = "https://github.com/RedVortexDev/BitFuzz"

        licenses {
            license {
                name = "Apache-2.0"
                url = "https://www.apache.org/licenses/LICENSE-2.0"
            }
        }

        developers {
            developer {
                id = "RedVortexDev"
                name = "Red"
                url = "https://github.com/RedVortexDev"
            }
        }

        scm {
            url = "https://github.com/RedVortexDev/BitFuzz"
            connection = "scm:git:https://github.com/RedVortexDev/BitFuzz.git"
            developerConnection = "scm:git:ssh://git@github.com/RedVortexDev/BitFuzz.git"
        }
    }
}
