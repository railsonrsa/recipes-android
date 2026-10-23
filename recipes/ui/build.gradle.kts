plugins {
    alias(libs.plugins.recipes.android.library)
    alias(libs.plugins.recipes.android.compose)
}

android {
    namespace = "com.railson.recipes.recipes.ui"
}

dependencies {
    implementation(projects.recipes.domain)
    implementation(projects.core.ui)
}
