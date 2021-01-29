plugins {
    kotlin("jvm") version Deps.Version.kotlin
}

subprojects {
    version = "1.0.0"
}

dependencies {
    api(project(":core"))
    implementation(Deps.kotlin)
    implementation(Deps.coroutines)
}