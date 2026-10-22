package com.railson.recipes.feature.recipelist.testing

import com.railson.recipes.recipes.domain.model.CallOutcome
import com.railson.recipes.recipes.domain.model.SearchQuery
import com.railson.recipes.recipes.domain.model.SearchResult
import com.railson.recipes.recipes.domain.repository.RecipeRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** Holds a saved result tests can change. Each refresh runs until the test ends it with [endRefresh]. */
internal class FakeRecipeRepository : RecipeRepository {
    val saved = MutableStateFlow<SearchResult?>(null)
    val refreshed = mutableListOf<SearchQuery>()
    private val outcomes = Channel<CallOutcome>(Channel.UNLIMITED)

    fun endRefresh(outcome: CallOutcome) {
        outcomes.trySend(outcome)
    }

    override fun observeSearch(query: SearchQuery): Flow<SearchResult?> = saved

    override suspend fun refresh(query: SearchQuery): CallOutcome {
        refreshed += query
        return outcomes.receive()
    }
}
