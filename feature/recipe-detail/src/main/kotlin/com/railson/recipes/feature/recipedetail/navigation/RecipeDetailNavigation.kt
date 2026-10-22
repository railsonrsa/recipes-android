package com.railson.recipes.feature.recipedetail.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.railson.recipes.feature.recipedetail.detail.RecipeDetailScreen
import kotlinx.serialization.Serializable

fun NavGraphBuilder.recipeDetailScreen(onBack: () -> Unit) {
    composable<RecipeDetailRoute> { entry ->
        RecipeDetailScreen(id = entry.toRoute<RecipeDetailRoute>().id, onBack = onBack)
    }
}

@Serializable
data class RecipeDetailRoute(val id: String)
