plugins {
    alias(libs.plugins.recipes.android.library)
}

android {
    namespace = "com.railson.recipes.core.network"
    buildFeatures.buildConfig = true
}

dependencies {
    api(libs.okhttp)
    api(libs.retrofit)
    api(libs.kotlinx.serialization.json)
    implementation(libs.okhttp.logging)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)
}
