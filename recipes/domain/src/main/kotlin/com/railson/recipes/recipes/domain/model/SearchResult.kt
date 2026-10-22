package com.railson.recipes.recipes.domain.model

/** The recipes that match a search so far, and whether more are left to load. */
data class SearchResult(
    val recipes: List<Recipe>,
    val hasMore: Boolean,
)
