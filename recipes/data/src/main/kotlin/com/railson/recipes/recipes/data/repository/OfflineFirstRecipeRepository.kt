package com.railson.recipes.recipes.data.repository

import com.railson.recipes.recipes.data.local.dao.RecipeDao
import com.railson.recipes.recipes.data.local.dao.RecipeSearchDao
import com.railson.recipes.recipes.data.local.entity.RecipeEntity
import com.railson.recipes.recipes.data.local.entity.SearchRemoteKeyEntity
import com.railson.recipes.recipes.data.mapper.toDomain
import com.railson.recipes.recipes.data.mapper.toEntity
import com.railson.recipes.recipes.data.remote.RecipePageDto
import com.railson.recipes.recipes.data.remote.RecipesApi
import com.railson.recipes.recipes.domain.model.CallOutcome
import com.railson.recipes.recipes.domain.model.Recipe
import com.railson.recipes.recipes.domain.model.SearchQuery
import com.railson.recipes.recipes.domain.model.SearchResult
import com.railson.recipes.recipes.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlin.time.Clock

/** The UI reads Room only. The network just fills Room. See docs/specs/data.md. */
internal class OfflineFirstRecipeRepository(
    private val api: RecipesApi,
    private val recipeSearchDao: RecipeSearchDao,
    private val recipeDao: RecipeDao,
    private val pageSize: Int = PAGE_SIZE,
) : RecipeRepository {

    override fun observeSearch(query: SearchQuery): Flow<SearchResult?> = combine(
        recipeSearchDao.observeRecipes(query.searchKey),
        recipeSearchDao.observeRemoteKey(query.searchKey),
    ) { recipes: List<RecipeEntity>, remoteKey: SearchRemoteKeyEntity? ->
        // The remote key is written with every save, so no key means the search was never saved.
        remoteKey?.let {
            SearchResult(
                recipes = recipes.map { recipe -> recipe.toDomain() },
                hasMore = it.nextPage != null,
                totalItems = it.totalItems,
            )
        }
    }

    override fun observeRecipe(id: String): Flow<Recipe?> = recipeDao.observeRecipe(id).map { it?.toDomain() }

    override suspend fun refresh(query: SearchQuery): CallOutcome = callServer {
        val page = api.getRecipes(query, page = 1, pageSize = pageSize)
        recipeSearchDao.replaceSearch(remoteKey = page.remoteKey(query), recipes = page.data.map { it.toEntity() })
    }

    override suspend fun loadNextPage(query: SearchQuery): CallOutcome {
        val nextPage = recipeSearchDao.observeRemoteKey(query.searchKey).first()?.nextPage ?: return CallOutcome.Success
        return callServer {
            val page = api.getRecipes(query, page = nextPage, pageSize = pageSize)
            recipeSearchDao.appendPage(remoteKey = page.remoteKey(query), recipes = page.data.map { it.toEntity() })
        }
    }

    override suspend fun count(query: SearchQuery): Int? {
        var totalItems: Int? = null
        val outcome = callServer { totalItems = api.getRecipes(query, page = 1, pageSize = 1).pagination.totalItems }
        return totalItems.takeIf { outcome == CallOutcome.Success }
    }

    private companion object {
        const val PAGE_SIZE = 10
    }
}

private fun RecipePageDto.remoteKey(query: SearchQuery) = SearchRemoteKeyEntity(
    searchKey = query.searchKey,
    nextPage = pagination.nextPage,
    totalItems = pagination.totalItems,
    lastUpdated = Clock.System.now().toEpochMilliseconds(),
)

private suspend fun RecipesApi.getRecipes(query: SearchQuery, page: Int, pageSize: Int): RecipePageDto =
    with(query.searchParams) {
        getRecipes(
            text = text,
            diet = diet,
            minServings = minServings,
            maxServings = maxServings,
            difficulties = difficulties,
            include = include,
            exclude = exclude,
            instructions = instructions,
            page = page,
            pageSize = pageSize,
        )
    }
