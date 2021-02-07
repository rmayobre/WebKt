plugins {
    kotlin("jvm") version Deps.Version.kotlin
}

group = "io.webkt"

repositories {
    mavenCentral()
    jcenter()
}
// This code will enable inline class
//
/*
val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    freeCompilerArgs = listOf("-Xinline-classes")
}
*/