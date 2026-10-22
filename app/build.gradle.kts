plugins {
    alias(libs.plugins.recipes.android.application)
    alias(libs.plugins.recipes.android.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.railson.recipes"

    defaultConfig {
        applicationId = "com.railson.recipes"
        versionCode = 1
        versionName = "0.1.0"
    }
}

dependencies {
    implementation(projects.core.ui)
    implementation(projects.core.network)
    implementation(projects.recipes.domain)
    implementation(projects.recipes.data)
    implementation(projects.feature.recipeList)
    implementation(projects.feature.recipeDetail)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.json)

    implementation(platform(libs.koin.bom))
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
}
