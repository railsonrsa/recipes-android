package com.railson.recipes.recipes.data.repository

import com.railson.recipes.recipes.data.search.normalizedForSearch
import com.railson.recipes.recipes.domain.model.SearchQuery

/** The search's key in Room: the same search always gives the same key. See docs/specs/data.md, "Cache (Room)". */
internal val SearchQuery.searchKey: String
    get() = text.normalizedForSearch().let { if (it.isEmpty()) "" else "q=$it" }
