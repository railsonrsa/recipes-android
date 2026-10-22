package com.railson.recipes.recipes.data.repository

import com.railson.recipes.recipes.data.local.dao.SearchDao
import com.railson.recipes.recipes.data.local.entity.RecipeEntity
import com.railson.recipes.recipes.data.local.entity.SearchRemoteKeyEntity
import com.railson.recipes.recipes.data.mapper.toDomain
import com.railson.recipes.recipes.data.mapper.toEntity
import com.railson.recipes.recipes.data.remote.RecipesApi
import com.railson.recipes.recipes.domain.model.CallOutcome
import com.railson.recipes.recipes.domain.model.SearchQuery
import com.railson.recipes.recipes.domain.model.SearchResult
import com.railson.recipes.recipes.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlin.time.Clock

/** The UI reads Room only. The network just fills Room. See docs/specs/data.md. */
internal class OfflineFirstRecipeRepository(
    private val api: RecipesApi,
    private val dao: SearchDao,
) : RecipeRepository {

    override fun observeSearch(query: SearchQuery): Flow<SearchResult?> = combine(
        dao.observeRecipes(query.searchKey),
        dao.observeRemoteKey(query.searchKey),
    ) { recipes: List<RecipeEntity>, remoteKey: SearchRemoteKeyEntity? ->
        // The remote key is written with every save, so no key means the search was never saved.
        remoteKey?.let { SearchResult(recipes = recipes.map { it.toDomain() }, hasMore = it.nextPage != null) }
    }

    override suspend fun refresh(query: SearchQuery): CallOutcome = callServer {
        val page = api.getRecipes(page = 1, pageSize = PAGE_SIZE)
        dao.replaceSearch(
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
