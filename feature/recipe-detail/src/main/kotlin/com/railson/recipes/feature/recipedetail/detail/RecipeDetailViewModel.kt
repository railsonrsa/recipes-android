package com.railson.recipes.feature.recipedetail.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.railson.recipes.recipes.domain.model.Recipe
import com.railson.recipes.recipes.domain.model.ScaledServingsRange
import com.railson.recipes.recipes.domain.model.scaledIngredients
import com.railson.recipes.recipes.domain.usecase.ObserveRecipeUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/** Shows the saved recipe and scales its ingredients. It reads Room only, so it works offline. */
internal class RecipeDetailViewModel(
    id: String,
    observeRecipe: ObserveRecipeUseCase,
) : ViewModel() {

    /** `null` keeps the servings as written. */
    private val scaledServings = MutableStateFlow<Int?>(null)

    val uiState: StateFlow<RecipeDetailUiState> = combine(observeRecipe(id), scaledServings, ::screenState)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), RecipeDetailUiState.Loading)

    fun onAction(action: RecipeDetailAction) {
        val content = uiState.value as? RecipeDetailUiState.Content ?: return
        scaledServings.value = when (action) {
            RecipeDetailAction.MoreServings ->
                if (content.canAddServings) content.scaledServings + 1 else content.scaledServings
            RecipeDetailAction.FewerServings ->
                if (content.canRemoveServings) content.scaledServings - 1 else content.scaledServings
            RecipeDetailAction.ResetServings -> null
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}

private fun screenState(recipe: Recipe?, scaledServings: Int?): RecipeDetailUiState {
    if (recipe == null) return RecipeDetailUiState.NotAvailable
    val servings = scaledServings ?: recipe.servings
    return RecipeDetailUiState.Content(
        recipe = recipe,
        scaledServings = servings,
        ingredients = recipe.scaledIngredients(servings),
    )
}
