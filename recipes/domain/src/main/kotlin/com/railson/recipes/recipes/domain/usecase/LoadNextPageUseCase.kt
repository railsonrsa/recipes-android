package com.railson.recipes.recipes.domain.usecase

import com.railson.recipes.recipes.domain.model.CallOutcome
import com.railson.recipes.recipes.domain.model.SearchQuery
import com.railson.recipes.recipes.domain.repository.RecipeRepository

class LoadNextPageUseCase(private val repository: RecipeRepository) {
    suspend operator fun invoke(query: SearchQuery): CallOutcome = repository.loadNextPage(query)
}
