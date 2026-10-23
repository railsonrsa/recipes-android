package com.railson.recipes.recipes.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.railson.recipes.recipes.data.local.entity.RecipeEntity
import com.railson.recipes.recipes.data.local.entity.SearchRemoteKeyEntity
import com.railson.recipes.recipes.data.local.entity.SearchResultEntity
import kotlinx.coroutines.flow.Flow

@Dao
internal abstract class RecipeSearchDao {

    @Query(
        """
        SELECT recipes.* FROM recipes
        JOIN search_results ON search_results.recipeId = recipes.id
        WHERE search_results.searchKey = :searchKey
        ORDER BY search_results.position
        """,
    )
    abstract fun observeRecipes(searchKey: String): Flow<List<RecipeEntity>>

    @Query("SELECT * FROM search_remote_keys WHERE searchKey = :searchKey")
    abstract fun observeRemoteKey(searchKey: String): Flow<SearchRemoteKeyEntity?>

    /** Refresh: the search's saved result becomes page 1. Recipe rows are never deleted. */
    @Transaction
    open suspend fun replaceSearch(remoteKey: SearchRemoteKeyEntity, recipes: List<RecipeEntity>) {
        upsertRecipes(recipes)
        deleteResults(remoteKey.searchKey)
        deleteRemoteKey(remoteKey.searchKey)
        insertResults(
            recipes.mapIndexed { position, recipe ->
                SearchResultEntity(searchKey = remoteKey.searchKey, position = position, recipeId = recipe.id)
            },
        )
        insertRemoteKey(remoteKey)
    }

    /** Next page: its recipes go after the last saved position, and the remote key moves on. */
    @Transaction
    open suspend fun appendPage(remoteKey: SearchRemoteKeyEntity, recipes: List<RecipeEntity>) {
        upsertRecipes(recipes)
        val start = (lastPosition(remoteKey.searchKey) ?: -1) + 1
        insertResults(
            recipes.mapIndexed { index, recipe ->
                SearchResultEntity(searchKey = remoteKey.searchKey, position = start + index, recipeId = recipe.id)
            },
        )
        deleteRemoteKey(remoteKey.searchKey)
        insertRemoteKey(remoteKey)
    }

    @Query("SELECT MAX(position) FROM search_results WHERE searchKey = :searchKey")
    protected abstract suspend fun lastPosition(searchKey: String): Int?

    @Upsert
    protected abstract suspend fun upsertRecipes(recipes: List<RecipeEntity>)

    @Query("DELETE FROM search_results WHERE searchKey = :searchKey")
    protected abstract suspend fun deleteResults(searchKey: String)

    @Query("DELETE FROM search_remote_keys WHERE searchKey = :searchKey")
    protected abstract suspend fun deleteRemoteKey(searchKey: String)

    @Insert
    protected abstract suspend fun insertResults(results: List<SearchResultEntity>)

    @Insert
    protected abstract suspend fun insertRemoteKey(remoteKey: SearchRemoteKeyEntity)
}
