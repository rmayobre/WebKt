plugins {
    kotlin("jvm") version Deps.Version.kotlin
}

repositories {
    mavenCentral()
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")

    group = "io.webkt"

    repositories {
        mavenCentral()
        jcenter()
    }
}