plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "BitFuzz"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

include("buffer")
include("buffer-netty")
include("buffer-nio")

include("schema-api")
include("schema-core")
include("schema-std")

include("fuzz-api")
include("fuzz-core")
