object Deps {
    // Kotlin dependencies
    const val kotlin = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Version.kotlin}"
    const val kotlinGradle = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Version.kotlin}"
    const val coroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Version.coroutines}"

    // Unit testing dependencies
    const val mockk =  "io.mockk:mockk:${Version.mockk}"
    const val junit = "junit:junit:${Version.junit}"

    object Version {
        // Kotlin library versions
        const val kotlin = "1.4.21"
        const val coroutines = "1.4.2"

        // Unit testing versions
        const val mockk = "1.10.5"
        const val junit = "4.12"
    }
}