plugins {
    kotlin("jvm") version Deps.Version.kotlin
}

group = "io.webkt"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    api(project(":http-client"))
    api(project(":websocket"))
    implementation(Deps.kotlin)
}