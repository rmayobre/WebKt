package plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.plugins.PluginContainer
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.dependencies

abstract class WebKtPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(plugins) {
                plugins()
            }
            dependencies {
                dependencies()
            }
        }
    }

    /**
     * Apply plugins to the project.
     * @see [PluginContainer.apply]
     */
    protected abstract fun PluginContainer.plugins()

    /**
     * Configure dependencies to the project.
     * @see api
     * @see implementation
     * @see testImplementation
     */
    protected abstract fun DependencyHandlerScope.dependencies()

    /**
     * Adds a dependency to the 'api' configuration.
     */
    protected fun DependencyHandlerScope.api(dependency: String) {
        api(dependencyNotation = dependency)
    }

    /**
     * Adds a dependency to the 'api' configuration.
     */
    protected fun DependencyHandlerScope.api(dependency: ProjectDependency) {
        api(dependencyNotation = dependency)
    }

    /**
     * Adds a dependency to the 'implementation' configuration.
     */
    protected fun DependencyHandlerScope.implementation(dependency: String) {
        implementation(dependencyNotation = dependency)
    }

    /**
     * Adds a dependency to the 'implementation' configuration.
     */
    protected fun DependencyHandlerScope.implementation(dependency: ProjectDependency) {
        implementation(dependencyNotation = dependency)
    }

    /**
     * Adds a dependency to the 'implementation' configuration.
     */
    protected fun DependencyHandlerScope.testImplementation(dependency: String) {
        testImplementation(dependencyNotation = dependency)
    }

    /**
     * Adds a dependency to the 'implementation' configuration.
     */
    protected fun DependencyHandlerScope.testImplementation(dependency: ProjectDependency) {
        testImplementation(dependencyNotation = dependency)
    }

    companion object {
        private const val api = "api"
        private const val implementation = "implementation"
        private const val testImplementation = "implementation"
    }
}