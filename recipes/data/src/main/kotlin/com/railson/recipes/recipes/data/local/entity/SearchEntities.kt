package com.railson.recipes.recipes.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** One recipe a search returned, at its place in the result. */
@Entity(tableName = "search_results", primaryKeys = ["searchKey", "position"])
internal data class SearchResultEntity(
    val searchKey: String,
    val position: Int,
    val recipeId: String,
)

/** Paging and freshness of one search. */
@Entity(tableName = "search_remote_keys")
internal data class SearchRemoteKeyEntity(
    @PrimaryKey val searchKey: String,
    val nextPage: Int?,
    val totalItems: Int,
    val lastUpdated: Long,
)
