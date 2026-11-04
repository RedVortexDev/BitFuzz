plugins {
    id("bitfuzz.java-library")
}

dependencies {
    api(project(":buffer"))
    api(libs.netty.buffer)

    testImplementation(testFixtures(project(":buffer")))
}
