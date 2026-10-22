package com.railson.recipes.feature.recipelist.list

/** What the reader does on the list screen. */
internal sealed interface RecipeListAction {
    /** Pull-to-refresh, "Try again", "Refresh" and every "Retry". */
    data object Refresh : RecipeListAction
}
