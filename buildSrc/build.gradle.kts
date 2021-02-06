plugins {
//    kotlin("jvm") version "1.4.20"
    `kotlin-dsl`
}

repositories {
    jcenter()
    mavenCentral()
}

//dependencies {
//    implementation(kotlin("stdlib"))
//}

gradlePlugin {
    plugins {
        register("WebKt-Common-Plugin") {
            id = "WebKt-Common-Plugin"
            implementationClass = "io.webkt.plugin.WebKtCommonPlugin"
        }
    }
}