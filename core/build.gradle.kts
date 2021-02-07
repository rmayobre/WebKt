plugins {
//    kotlin("jvm") version Deps.Version.kotlin
//    `kotlin`
//    `WebKt-Library`
    `webkt-library`
}
//apply(plugin = )

group = "io.webkt"
version = "1.0.0"

repositories {
    mavenCentral()
}

//dependencies {
//    implementation(Deps.kotlin)
//    implementation(Deps.coroutines)
//    testImplementation(Deps.junit)
//    testImplementation(Deps.mockk)
//}
//
// This code will enable inline class
//
/*
val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    freeCompilerArgs = listOf("-Xinline-classes")
}
*/