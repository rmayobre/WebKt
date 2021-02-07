plugins {
    `java-library`
}

group = "io.webkt"

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation(Deps.kotlin)
//    implementation(Deps.kotlinGradle)
    implementation(Deps.coroutines)
    testImplementation(Deps.junit)
    testImplementation(Deps.mockk)
}