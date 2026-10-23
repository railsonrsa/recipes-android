package com.railson.recipes.feature.recipedetail.detail

import com.railson.recipes.recipes.domain.model.Ingredient
import com.railson.recipes.recipes.domain.model.Recipe
import com.railson.recipes.recipes.domain.model.ScaledServingsRange

/** The detail screen's states. */
internal sealed interface RecipeDetailUiState {
    /** Until Room answers. */
    data object Loading : RecipeDetailUiState

    /** The id was never saved (docs/specs/architecture.md, Recipe not in Room). */
    data object NotAvailable : RecipeDetailUiState

    /** The recipe, with [ingredients] scaled to [scaledServings]. */
    data class Content(
        val recipe: Recipe,
        val scaledServings: Int,
        val ingredients: List<Ingredient>,
    ) : RecipeDetailUiState {
        val isScaled: Boolean get() = scaledServings != recipe.servings
        val canAddServings: Boolean get() = scaledServings < ScaledServingsRange.last
        val canRemoveServings: Boolean get() = scaledServings > ScaledServingsRange.first
    }
}
