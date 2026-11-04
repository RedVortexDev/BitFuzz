plugins {
    id("bitfuzz.java-library")
    `java-test-fixtures`
}

dependencies {
    testFixturesApi(platform(libs.junit.bom))
    testFixturesApi(libs.junit.jupiter)
    testFixturesApi(libs.assertj.core)
}
