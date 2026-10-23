package com.railson.recipes.recipes.data.repository

import com.railson.recipes.recipes.domain.model.SearchQuery

/**
 * The search's key in Room: the same search always gives the same key. See docs/specs/data.md, "Cache (Room)".
 * It reads like the endpoint's query parameters, in the order of that table.
 */
internal val SearchQuery.searchKey: String
    get() = with(searchParams) {
        buildList {
            text?.let { add("q=$it") }
            diet.forEach { add("diet=$it") }
            minServings?.let { add("minServings=$it") }
            maxServings?.let { add("maxServings=$it") }
            difficulties.forEach { add("difficulty=$it") }
            include.forEach { add("include=$it") }
            exclude.forEach { add("exclude=$it") }
            instructions?.let { add("instructions=$it") }
        }.joinToString("&")
    }
