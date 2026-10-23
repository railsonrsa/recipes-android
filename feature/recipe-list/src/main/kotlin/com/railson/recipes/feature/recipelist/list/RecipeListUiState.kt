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

    /** A search with text or filters that matches no recipe. */
    data object NoResults : RecipeListUiState

    /**
     * The saved recipes. [isRefreshing] while a call to the server runs.
     * [totalItems] is how many the server has, loaded or not. [listEnd] is what comes after the last card.
     */
    data class Content(
        val recipes: List<Recipe>,
        val isRefreshing: Boolean,
        val totalItems: Int,
        val listEnd: ListEnd,
    ) : RecipeListUiState
}

/** What comes after the last card (DESIGN.md 3.3 and 3.4). */
internal enum class ListEnd {
    /** "Loading more recipes…". Coming into view loads the next page. */
    LoadingMore,

    /** "Couldn't load more" + "Retry". */
    LoadMoreFailed,

    /** "That's all N recipes". No more calls. */
    AllLoaded,
}
