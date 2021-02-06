plugins {
    kotlin("jvm") version Deps.Version.kotlin
}

subprojects {
    version = "1.0.0"

}

dependencies {
    implementation(project(":core"))
    implementation(Deps.kotlin)
    implementation(Deps.coroutines)
}
