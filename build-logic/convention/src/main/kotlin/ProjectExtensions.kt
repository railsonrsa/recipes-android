import com.android.build.api.dsl.CommonExtension
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension

internal val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

internal fun VersionCatalog.version(alias: String): String =
    findVersion(alias).get().requiredVersion

internal fun VersionCatalog.versionInt(alias: String): Int = version(alias).toInt()

internal fun VersionCatalog.library(alias: String): Provider<MinimalExternalModuleDependency> =
    findLibrary(alias).get()

internal fun Project.configureKotlin() {
    extensions.getByType<KotlinProjectExtension>().jvmToolchain(libs.versionInt("jvmToolchain"))
    configureDetekt()
    configureUnitTests()
}

internal fun Project.configureAndroid(android: CommonExtension<*, *, *, *, *, *>) {
    android.apply {
        compileSdk = libs.versionInt("compileSdk")
        defaultConfig {
            minSdk = libs.versionInt("minSdk")
        }
        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_17
            targetCompatibility = JavaVersion.VERSION_17
        }
        lint {
            targetSdk = libs.versionInt("targetSdk")
            configureLint()
        }
    }
    configureKotlin()
}

internal fun com.android.build.api.dsl.Lint.configureLint() {
    abortOnError = true
    warningsAsErrors = true
    // Versions and SDK levels are pinned on purpose (see libs.versions.toml). These checks would break
    // the build each time a library or an Android version ships.
    disable += setOf("AndroidGradlePluginVersion", "GradleDependency", "NewerVersionAvailable", "OldTargetApi")
}

private fun Project.configureDetekt() {
    pluginManager.apply("io.gitlab.arturbosch.detekt")
    extensions.configure<DetektExtension> {
        buildUponDefaultConfig = true
        config.setFrom(rootProject.file("config/detekt/detekt.yml"))
        // Every source set, so the debug-only code is checked too.
        source.setFrom(files("src"))
    }
    val detektKotlin = libs.version("detektKotlin")
    configurations.matching { it.name == "detekt" }.configureEach {
        resolutionStrategy.eachDependency {
            if (requested.group == "org.jetbrains.kotlin") useVersion(detektKotlin)
        }
    }
}

private fun Project.configureUnitTests() {
    dependencies {
        add("testImplementation", libs.library("junit"))
        add("testImplementation", libs.library("truth"))
        add("testImplementation", libs.library("mockk"))
        add("testImplementation", libs.library("kotlinx-coroutines-test"))
        add("testImplementation", libs.library("turbine"))
    }
}
