package com.railson.recipes.feature.recipedetail.detail

/** What the reader does on the detail screen. */
internal sealed interface RecipeDetailAction {
    /** The stepper's "+". */
    data object MoreServings : RecipeDetailAction

    /** The stepper's "−". */
    data object FewerServings : RecipeDetailAction

    /** "Reset", back to the servings as written. */
    data object ResetServings : RecipeDetailAction
}
