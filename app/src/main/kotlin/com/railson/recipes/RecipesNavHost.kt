package com.railson.recipes

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.railson.recipes.feature.recipedetail.navigation.RecipeDetailRoute
import com.railson.recipes.feature.recipedetail.navigation.recipeDetailScreen
import com.railson.recipes.feature.recipelist.navigation.RecipeListRoute
import com.railson.recipes.feature.recipelist.navigation.recipeListScreen

@Composable
internal fun RecipesNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = RecipeListRoute) {
        recipeListScreen(
            onRecipeClick = { id -> navController.navigate(RecipeDetailRoute(id)) },
            topBarActions = { TopBarActions() },
        )
        recipeDetailScreen(onBack = navController::navigateUp)
    }
}
