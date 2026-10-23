package com.railson.recipes.recipes.data.repository

import com.railson.recipes.recipes.data.mapper.key
import com.railson.recipes.recipes.data.mapper.number
import com.railson.recipes.recipes.data.search.normalizedForSearch
import com.railson.recipes.recipes.domain.model.SearchQuery

/**
 * The search's key in Room: the same search always gives the same key. See docs/specs/data.md, "Cache (Room)".
 * It reads like the endpoint's query parameters, in a fixed order with each list sorted.
 */
internal val SearchQuery.searchKey: String
    get() = buildList {
        text.normalizedForSearch().takeIf { it.isNotEmpty() }?.let { add("q=$it") }
        dietaryTags.map { it.key }.sorted().forEach { add("diet=$it") }
        servingsRange.min?.let { add("minServings=$it") }
        servingsRange.max?.let { add("maxServings=$it") }
        difficulties.map { it.number }.sorted().forEach { add("difficulty=$it") }
    }.joinToString("&")
