import org.gradle.plugin.use.PluginDependenciesSpec
import org.gradle.plugin.use.PluginDependencySpec
import plugin.WebKtLibrary

/**
 * Apply the WebKtLibrary plugin to the project.
 * @see WebKtLibrary
 */
inline val PluginDependenciesSpec.`WebKt-library`: PluginDependencySpec
    get() = id("WebKt-Library")