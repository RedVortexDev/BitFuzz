plugins {
    id("bitfuzz.java-library")
}

dependencies {
    api(project(":fuzz-api"))
    implementation(project(":schema-std"))
    implementation(project(":buffer-nio"))
}
