plugins {
    kotlin("jvm") version Deps.Version.kotlin
}

group = "io.webkt"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(Deps.kotlin)
}