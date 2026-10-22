package com.railson.recipes.recipes.domain.usecase

import com.railson.recipes.recipes.domain.model.SearchQuery
import com.railson.recipes.recipes.domain.model.SearchResult
import com.railson.recipes.recipes.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.Flow

class ObserveSearchUseCase(private val repository: RecipeRepository) {
    operator fun invoke(query: SearchQuery): Flow<SearchResult?> = repository.observeSearch(query)
}
