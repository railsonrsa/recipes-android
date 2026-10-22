plugins {
    alias(libs.plugins.recipes.android.library)
}

android {
    namespace = "com.railson.recipes.recipes.data"
}

dependencies {
    implementation(projects.recipes.domain)
    implementation(projects.core.network)
}
