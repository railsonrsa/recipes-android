package com.railson.recipes.feature.recipedetail.testing

import com.railson.recipes.recipes.domain.model.CallOutcome
import com.railson.recipes.recipes.domain.model.Recipe
import com.railson.recipes.recipes.domain.model.SearchQuery
import com.railson.recipes.recipes.domain.model.SearchResult
import com.railson.recipes.recipes.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

/** Holds the saved recipes by id. The detail screen never searches or calls the server. */
internal class FakeRecipeRepository : RecipeRepository {
    val saved = MutableStateFlow<Map<String, Recipe>>(emptyMap())

    override fun observeRecipe(id: String): Flow<Recipe?> = saved.map { it[id] }

    override fun observeSearch(query: SearchQuery): Flow<SearchResult?> = flowOf(null)

    override suspend fun refresh(query: SearchQuery): CallOutcome = error("The detail screen never refreshes")

    override suspend fun count(query: SearchQuery): Int = error("The detail screen never counts")
}
