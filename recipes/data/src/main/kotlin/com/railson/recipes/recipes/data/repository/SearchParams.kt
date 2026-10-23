package com.railson.recipes.recipes.data.repository

import com.railson.recipes.recipes.data.mapper.key
import com.railson.recipes.recipes.data.mapper.number
import com.railson.recipes.recipes.domain.model.normalizedForSearch
import com.railson.recipes.recipes.domain.model.SearchQuery

/**
 * The query parameters of `GET /recipes` for a search (docs/specs/data.md, "Query parameters"), built once
 * for both the call and the [searchKey]. Texts are normalized, lists sorted and without duplicates, and a
 * part that is off is `null` or empty, so Retrofit leaves it out.
 */
internal data class SearchParams(
    val text: String?,
    val diet: List<String>,
    val minServings: Int?,
    val maxServings: Int?,
    val difficulties: List<Int>,
    val include: List<String>,
    val exclude: List<String>,
    val instructions: String?,
)

internal val SearchQuery.searchParams: SearchParams
    get() = SearchParams(
        text = text.normalizedForSearch().ifEmpty { null },
        diet = dietaryTags.map { it.key }.sorted(),
        minServings = servingsRange.min,
        maxServings = servingsRange.max,
        difficulties = difficulties.map { it.number }.sorted(),
        include = includedIngredients.normalizedWords(),
        exclude = excludedIngredients.normalizedWords(),
        instructions = instructionSearch.normalizedForSearch().ifEmpty { null },
    )

private fun Set<String>.normalizedWords(): List<String> =
    map { it.normalizedForSearch() }.filter { it.isNotEmpty() }.distinct().sorted()
