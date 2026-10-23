package com.railson.recipes.recipes.data.repository

import com.railson.recipes.recipes.data.local.dao.RecipeDao
import com.railson.recipes.recipes.data.local.dao.RecipeSearchDao
import com.railson.recipes.recipes.data.local.entity.RecipeEntity
import com.railson.recipes.recipes.data.local.entity.SearchRemoteKeyEntity
import com.railson.recipes.recipes.data.mapper.toDomain
import com.railson.recipes.recipes.data.mapper.toEntity
import com.railson.recipes.recipes.data.remote.RecipesApi
import com.railson.recipes.recipes.domain.model.CallOutcome
import com.railson.recipes.recipes.domain.model.Recipe
import com.railson.recipes.recipes.domain.model.SearchQuery
import com.railson.recipes.recipes.domain.model.SearchResult
import com.railson.recipes.recipes.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlin.time.Clock

/** The UI reads Room only. The network just fills Room. See docs/specs/data.md. */
internal class OfflineFirstRecipeRepository(
    private val api: RecipesApi,
    private val recipeSearchDao: RecipeSearchDao,
    private val recipeDao: RecipeDao,
) : RecipeRepository {

    override fun observeSearch(query: SearchQuery): Flow<SearchResult?> = combine(
        recipeSearchDao.observeRecipes(query.searchKey),
        recipeSearchDao.observeRemoteKey(query.searchKey),
    ) { recipes: List<RecipeEntity>, remoteKey: SearchRemoteKeyEntity? ->
        // The remote key is written with every save, so no key means the search was never saved.
        remoteKey?.let { SearchResult(recipes = recipes.map { it.toDomain() }, hasMore = it.nextPage != null) }
    }

    override fun observeRecipe(id: String): Flow<Recipe?> = recipeDao.observeRecipe(id).map { it?.toDomain() }

    override suspend fun refresh(query: SearchQuery): CallOutcome = callServer {
        val page = api.getRecipes(page = 1, pageSize = PAGE_SIZE)
        recipeSearchDao.replaceSearch(
            remoteKey = SearchRemoteKeyEntity(
                searchKey = query.searchKey,
                nextPage = page.pagination.nextPage,
                totalItems = page.pagination.totalItems,
                lastUpdated = Clock.System.now().toEpochMilliseconds(),
            ),
            recipes = page.data.map { it.toEntity() },
        )
    }

    private companion object {
        const val PAGE_SIZE = 10
    }
}

/** Only the catalog for now, which is the empty search. Search (#23) builds the key from the text and filters. */
@Suppress("UnusedReceiverParameter") // The receiver is the search the key will be built from.
private val SearchQuery.searchKey: String get() = ""
