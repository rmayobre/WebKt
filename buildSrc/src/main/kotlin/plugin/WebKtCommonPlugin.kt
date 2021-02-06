package plugin

import org.gradle.api.plugins.PluginContainer
import org.gradle.kotlin.dsl.DependencyHandlerScope

class WebKtCommonPlugin : WebKtPlugin() {
    override fun PluginContainer.plugins() {
        // No plugins are implemented.
    }

    override fun DependencyHandlerScope.dependencies() {
        implementation(Deps.kotlin)
        implementation(Deps.coroutines)
        testImplementation(Deps.junit)
        testImplementation(Deps.mockk)
    }
}