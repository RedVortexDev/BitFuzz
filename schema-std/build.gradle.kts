plugins {
    id("bitfuzz.java-library")
}

dependencies {
    api(project(":schema-core"))

    testImplementation(project(":buffer-netty"))
}
