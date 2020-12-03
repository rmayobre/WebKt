plugins {
    kotlin("jvm") version Deps.Version.kotlin
    "java-library"
}

group = "io.webkt"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    api(project(":core"))
    implementation(Deps.kotlin)
    testImplementation(Deps.junit)
    testImplementation(Deps.mockk)
}