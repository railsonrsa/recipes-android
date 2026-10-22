plugins {
    alias(libs.plugins.recipes.android.library)
    alias(libs.plugins.recipes.android.compose)
}

android {
    namespace = "com.railson.recipes.core.ui"
}

dependencies {
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
}
