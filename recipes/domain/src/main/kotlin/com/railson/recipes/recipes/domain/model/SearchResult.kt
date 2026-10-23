package com.railson.recipes.recipes.domain.model

/**
 * The recipes that match a search so far, and whether more are left to load.
 * [totalItems] is how many the server has for the search, loaded or not.
 */
data class SearchResult(
    val recipes: List<Recipe>,
    val hasMore: Boolean,
    val totalItems: Int,
)
