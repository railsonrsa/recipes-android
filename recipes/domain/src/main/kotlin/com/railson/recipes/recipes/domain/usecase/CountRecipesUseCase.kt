package com.railson.recipes.recipes.domain.usecase

import com.railson.recipes.recipes.domain.model.SearchQuery
import com.railson.recipes.recipes.domain.repository.RecipeRepository

class CountRecipesUseCase(private val repository: RecipeRepository) {
    suspend operator fun invoke(query: SearchQuery): Int? = repository.count(query)
}
