plugins {
    kotlin("jvm") version Deps.Version.kotlin
    application
}

group = "io.webkt"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(Deps.kotlin)
    implementation(project(":core"))
//    implementation(project(":http-server"))
}