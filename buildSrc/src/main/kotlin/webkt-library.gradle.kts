import gradle.kotlin.dsl.accessors._566dc064233c60a31df379898f991b12.implementation
import gradle.kotlin.dsl.accessors._566dc064233c60a31df379898f991b12.testImplementation
import org.gradle.kotlin.dsl.`java-library`
import org.gradle.kotlin.dsl.dependencies

plugins {
    `java-library`
}

dependencies {
    with(WebKtLibraryLibs) {
        // Configures designated dependencies to "implementation".
        implementations.forEach {
            implementation(it)
        }
        // Configures designated dependencies to "testImplementation".
        testImplementations.forEach {
            testImplementation(it)
        }
    }
}