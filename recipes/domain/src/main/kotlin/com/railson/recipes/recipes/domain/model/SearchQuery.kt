package com.railson.recipes.recipes.domain.model

/** The search text plus the filters. The empty search is the whole catalog. The filters come with #24. */
data class SearchQuery(val text: String = "") {
    companion object {
        val Catalog = SearchQuery()
    }
}
