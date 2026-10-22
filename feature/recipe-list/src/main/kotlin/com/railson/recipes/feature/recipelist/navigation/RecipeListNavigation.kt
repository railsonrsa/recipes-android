package com.railson.recipes.feature.recipelist.navigation

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.railson.recipes.feature.recipelist.list.RecipeListScreen
import kotlinx.serialization.Serializable

fun NavGraphBuilder.recipeListScreen(topBarActions: @Composable RowScope.() -> Unit) {
    composable<RecipeListRoute> {
        RecipeListScreen(topBarActions = topBarActions)
    }
}

@Serializable
data object RecipeListRoute
