package com.railson.recipes.feature.recipelist.testing

import com.railson.recipes.recipes.domain.model.CallOutcome
import com.railson.recipes.recipes.domain.model.SearchQuery
import com.railson.recipes.recipes.domain.model.SearchResult
import com.railson.recipes.recipes.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** Holds a saved result tests can change, and counts the refreshes. */
internal class FakeRecipeRepository : RecipeRepository {
    val saved = MutableStateFlow(SearchResult(recipes = emptyList(), hasMore = false))
    val refreshed = mutableListOf<SearchQuery>()

    override fun observeSearch(query: SearchQuery): Flow<SearchResult> = saved

    override suspend fun refresh(query: SearchQuery): CallOutcome {
        refreshed += query
        return CallOutcome.Success
    }
}
