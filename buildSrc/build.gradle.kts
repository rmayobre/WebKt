plugins {
    `kotlin-dsl`
    `kotlin-dsl-precompiled-script-plugins`
}

repositories {
    jcenter()
    mavenCentral()
}

dependencies {
    implementation(Deps.kotlin)
    implementation(Deps.kotlinGradle)
}

//gradlePlugin {
//    plugins {
//        register("WebKt-Library") {
//            id = "WebKt-Library"
//            implementationClass = "plugin.WebKtLibrary"
//        }
//    }
//}