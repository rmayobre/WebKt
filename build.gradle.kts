
plugins {
    kotlin("jvm") version Lib.Version.kotlin
}

version = "1.0.0-ALPHA"

allprojects {
    group = "io.webkt"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")

    dependencies {
        implementation(kotlin("stdlib"))
    }
}
