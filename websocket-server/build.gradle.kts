plugins {
    kotlin("jvm") version Deps.Version.kotlin
    `java-library`
}

group = "io.webkt"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    api(project(":websocket"))
//    api(project(":http-server"))
    implementation(Deps.kotlin)
}