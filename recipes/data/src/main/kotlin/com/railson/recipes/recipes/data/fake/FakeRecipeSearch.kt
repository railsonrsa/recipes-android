package com.railson.recipes.recipes.data.fake

import com.railson.recipes.recipes.data.remote.PaginationDto
import com.railson.recipes.recipes.data.remote.RecipeDto
import com.railson.recipes.recipes.data.remote.RecipePageDto
import com.railson.recipes.recipes.data.search.normalizedForSearch

/**
 * What a call to `GET /recipes` asked for, named as in docs/specs/data.md, "Query parameters".
 * [text] is `q`, [diet] the tag keys, [difficulties] `1` to `3`. The ingredient filters come with #25.
 */
internal data class FakeSearchParams(
    val page: Int,
    val pageSize: Int,
    val text: String? = null,
    val diet: Set<String> = emptySet(),
    val minServings: Int? = null,
    val maxServings: Int? = null,
    val difficulties: Set<Int> = emptySet(),
)

/**
 * The fake server's answer to `GET /recipes`, without any HTTP: filter, then page what is left.
 *
 * `pagination` is built from the given list, so the asset's own `pagination` is ignored:
 * it only describes the full catalog on page 1.
 */
internal object FakeRecipeSearch {
    fun run(catalog: List<RecipeDto>, params: FakeSearchParams): RecipePageDto {
        val (page, pageSize) = params
        val recipes = catalog.filter { it.matches(params) }
        val totalPages = (recipes.size + pageSize - 1) / pageSize
        val hasNext = page < totalPages
        return RecipePageDto(
            data = recipes.drop((page - 1) * pageSize).take(pageSize),
            pagination = PaginationDto(
                page = page,
                pageSize = pageSize,
                totalItems = recipes.size,
                totalPages = totalPages,
                hasNext = hasNext,
                nextPage = if (hasNext) page + 1 else null,
                previousPage = if (page > 1) page - 1 else null,
            ),
        )
    }
}

private fun RecipeDto.matches(params: FakeSearchParams): Boolean =
    matchesText(params.text) &&
        dietaryAttributes.containsAll(params.diet) &&
        servings >= (params.minServings ?: servings) &&
        servings <= (params.maxServings ?: servings) &&
        (params.difficulties.isEmpty() || difficulty in params.difficulties)

private fun RecipeDto.matchesText(text: String?): Boolean {
    val normalized = text?.normalizedForSearch().orEmpty()
    return normalized in title.normalizedForSearch() || normalized in description.normalizedForSearch()
}
