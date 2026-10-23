package com.railson.recipes.recipes.domain.repository

import com.railson.recipes.recipes.domain.model.CallOutcome
import com.railson.recipes.recipes.domain.model.Recipe
import com.railson.recipes.recipes.domain.model.SearchQuery
import com.railson.recipes.recipes.domain.model.SearchResult
import kotlinx.coroutines.flow.Flow

interface RecipeRepository {
    /** Reads the saved result only, `null` while the search was never saved. Never calls the server. */
    fun observeSearch(query: SearchQuery): Flow<SearchResult?>

    /** Reads one saved recipe, `null` when it was never saved. Never calls the server. */
    fun observeRecipe(id: String): Flow<Recipe?>

    /** Asks the server for page 1 and replaces the saved result of this search. */
    suspend fun refresh(query: SearchQuery): CallOutcome

    /** How many recipes the server has for this search, `null` when the call fails. Saves nothing. */
    suspend fun count(query: SearchQuery): Int?
}
