package com.railson.recipes.feature.recipelist.list

import com.railson.recipes.recipes.domain.model.Recipe

internal data class RecipeListUiState(
    val recipes: List<Recipe> = emptyList(),
)
