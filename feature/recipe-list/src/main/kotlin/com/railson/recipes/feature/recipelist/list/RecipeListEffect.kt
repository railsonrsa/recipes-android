package com.railson.recipes.feature.recipelist.list

/** What the list screen shows once. */
internal sealed interface RecipeListEffect {
    /** "You're offline. Showing saved recipes." · "Retry" */
    data object ShowOfflineSnackbar : RecipeListEffect

    /** "Couldn't refresh recipes." · "Retry" */
    data object ShowRefreshFailedSnackbar : RecipeListEffect
}
