plugins {
    `kotlin-dsl`
}

repositories {
    jcenter()
    mavenCentral()
}

gradlePlugin {
    plugins {
        register("WebKt-Library") {
            id = "WebKt-Library"
            implementationClass = "plugin.WebKtLibrary"
        }
    }
}