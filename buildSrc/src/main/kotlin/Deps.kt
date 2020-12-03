object Deps {
    const val kotlin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Version.kotlin}"
    const val mockk =  "io.mockk:mockk:${Version.mockk}"
    const val junit = "junit:junit:${Version.junit}"

    object Version {
        const val kotlin = "1.4.20"
        const val mockk = "1.10.3-jdk8"
        const val junit = "4.12"
    }
}