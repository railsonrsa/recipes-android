import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.project

/** A feature module sees only `:recipes:domain`, `:recipes:ui`, `:core:ui` and `:core:format` (architecture.md, Dependency rules). */
class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("recipes.android.library")
        pluginManager.apply("recipes.android.compose")
        pluginManager.apply("org.jetbrains.kotlin.plugin.serialization")

        dependencies {
            add("implementation", project(":recipes:domain"))
            add("implementation", project(":recipes:ui"))
            add("implementation", project(":core:ui"))
            add("implementation", project(":core:format"))

            add("implementation", platform(libs.library("koin-bom")))
            add("implementation", libs.library("koin-androidx-compose"))
            add("implementation", libs.library("androidx-navigation-compose"))
            add("implementation", libs.library("androidx-lifecycle-runtime-compose"))
            add("implementation", libs.library("androidx-lifecycle-viewmodel-compose"))
            add("implementation", libs.library("kotlinx-serialization-json"))
        }
    }
}
