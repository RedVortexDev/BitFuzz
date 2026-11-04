plugins {
    id("bitfuzz.java-library")
}

dependencies {
    api(project(":buffer"))

    testImplementation(testFixtures(project(":buffer")))
}
