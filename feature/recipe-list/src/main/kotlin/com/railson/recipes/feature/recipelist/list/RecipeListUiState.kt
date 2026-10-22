package com.railson.recipes.feature.recipelist.list

import com.railson.recipes.recipes.domain.model.Recipe

/** The list screen's states. See docs/specs/data.md, "Screen states". */
internal sealed interface RecipeListUiState {
    data object Loading : RecipeListUiState

    /** Offline, with no saved recipes to show. */
    data object Offline : RecipeListUiState

    /** Any other failure, with no saved recipes to show. */
    data object Error : RecipeListUiState

    data object EmptyCatalogue : RecipeListUiState

    /** The saved recipes. [isRefreshing] while a call to the server runs. */
    data class Content(val recipes: List<Recipe>, val isRefreshing: Boolean) : RecipeListUiState
}
