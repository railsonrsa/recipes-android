package com.railson.recipes.feature.recipelist.list

/** What the reader does on the list screen. */
internal sealed interface RecipeListAction {
    /** Pull-to-refresh, "Try again", "Refresh" and every "Retry". */
    data object Refresh : RecipeListAction

    /** Types in the search bar. The search waits 300 ms after the last change. */
    data class ChangeSearchText(val text: String) : RecipeListAction

    /** Taps a recipe card. */
    data class OpenRecipe(val id: String) : RecipeListAction
}
