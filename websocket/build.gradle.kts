plugins {
    kotlin("jvm") version "1.4.20"
    `java-library`
}

group = "io.webkt"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    api(project(":http"))
    implementation(Deps.kotlin)
}