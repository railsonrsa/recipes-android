package com.railson.recipes.feature.recipelist.testing

import com.railson.recipes.recipes.domain.model.CallOutcome
import com.railson.recipes.recipes.domain.model.Recipe
import com.railson.recipes.recipes.domain.model.SearchQuery
import com.railson.recipes.recipes.domain.model.SearchResult
import com.railson.recipes.recipes.domain.repository.RecipeRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlin.time.Duration

/**
 * Holds a saved result per search that tests can change.
 * Each refresh runs until the test ends it with [endRefresh]. A count answers from [counts] after
 * [countWait], `null` (a failed call) for a search it doesn't have.
 */
internal class FakeRecipeRepository : RecipeRepository {
    private val searches = mutableMapOf<SearchQuery, MutableStateFlow<SearchResult?>>()
    val refreshed = mutableListOf<SearchQuery>()
    private val outcomes = Channel<CallOutcome>(Channel.UNLIMITED)
    val counts = mutableMapOf<SearchQuery, Int>()
    val counted = mutableListOf<SearchQuery>()
    var countWait: Duration = Duration.ZERO

    /** The saved catalog. */
    val saved: MutableStateFlow<SearchResult?> get() = savedFor(SearchQuery.Catalog)

    fun savedFor(query: SearchQuery): MutableStateFlow<SearchResult?> =
        searches.getOrPut(query) { MutableStateFlow(null) }

    fun endRefresh(outcome: CallOutcome) {
        outcomes.trySend(outcome)
    }

    override fun observeSearch(query: SearchQuery): Flow<SearchResult?> = savedFor(query)

    override fun observeRecipe(id: String): Flow<Recipe?> = flowOf(null)

    override suspend fun refresh(query: SearchQuery): CallOutcome {
        refreshed += query
        return outcomes.receive()
    }

    override suspend fun loadNextPage(query: SearchQuery): CallOutcome = CallOutcome.Success

    override suspend fun count(query: SearchQuery): Int? {
        counted += query
        delay(countWait)
        return counts[query]
    }
}
