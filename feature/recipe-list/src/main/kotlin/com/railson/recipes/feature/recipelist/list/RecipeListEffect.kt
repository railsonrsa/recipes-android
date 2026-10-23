package com.railson.recipes.feature.recipelist.list

import com.railson.recipes.recipes.domain.model.IngredientSide

/** What the list screen shows once. */
internal sealed interface RecipeListEffect {
    /** "You're offline. Showing saved recipes." · "Retry" */
    data object ShowOfflineSnackbar : RecipeListEffect

    /** "Couldn't refresh recipes." · "Retry" */
    data object ShowRefreshFailedSnackbar : RecipeListEffect

    /** "“garlic” moved to Must have", in the filter sheet. */
    data class ShowIngredientMoved(val word: String, val side: IngredientSide) : RecipeListEffect

    /** Leaves for the recipe's detail. The list doesn't know that screen, `:app` does. */
    data class OpenRecipe(val id: String) : RecipeListEffect
}
