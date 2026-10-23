package com.railson.recipes.feature.recipelist.filter

import com.railson.recipes.recipes.domain.model.SearchQuery

/**
 * The filters around the list: the [applied] ones (badge and chips), and the [draft] the sheet edits until
 * "Show N recipes". [draftCount] is the server's count for the draft, `null` until known or when the call fails.
 */
internal data class RecipeFiltersUiState(
    val applied: SearchQuery = SearchQuery.Catalog,
    val draft: SearchQuery = SearchQuery.Catalog,
    val draftCount: Int? = null,
)
