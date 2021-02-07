package plugin

import org.gradle.api.plugins.PluginContainer
import org.gradle.kotlin.dsl.DependencyHandlerScope

class WebKtLibrary : WebKtPlugin() {

    override fun PluginContainer.plugins() {
        apply("org.gradle.java-library")
    }

    override fun DependencyHandlerScope.dependencies() {
        implementation(Deps.kotlin)
        implementation(Deps.coroutines)
        testImplementation(Deps.junit)
        testImplementation(Deps.mockk)
    }
}