package com.railson.recipes.recipes.domain.usecase

import com.railson.recipes.recipes.domain.model.Recipe
import com.railson.recipes.recipes.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.Flow

class ObserveRecipeUseCase(private val repository: RecipeRepository) {
    operator fun invoke(id: String): Flow<Recipe?> = repository.observeRecipe(id)
}
