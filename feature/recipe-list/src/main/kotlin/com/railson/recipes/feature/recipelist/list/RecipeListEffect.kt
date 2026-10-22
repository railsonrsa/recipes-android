package com.railson.recipes.feature.recipelist.list

/** What the list screen shows once. */
internal sealed interface RecipeListEffect {
    /** "You're offline. Showing saved recipes." · "Retry" */
    data object ShowOfflineSnackbar : RecipeListEffect

    /** "Couldn't refresh recipes." · "Retry" */
    data object ShowRefreshFailedSnackbar : RecipeListEffect

    /** Leaves for the recipe's detail. The list doesn't know that screen, `:app` does. */
    data class OpenRecipe(val id: String) : RecipeListEffect
}
