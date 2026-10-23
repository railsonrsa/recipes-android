package com.railson.recipes.recipes.data.fake

import com.railson.recipes.recipes.data.remote.PaginationDto
import com.railson.recipes.recipes.data.remote.RecipeDto
import com.railson.recipes.recipes.data.remote.RecipePageDto
import com.railson.recipes.recipes.data.search.normalizedForSearch

/** What a call to `GET /recipes` asked for. [text] is the `q` parameter. The filters come with #24. */
internal data class FakeSearchParams(
    val page: Int,
    val pageSize: Int,
    val text: String? = null,
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

private fun RecipeDto.matches(params: FakeSearchParams): Boolean {
    val text = params.text?.normalizedForSearch().orEmpty()
    return text in title.normalizedForSearch() || text in description.normalizedForSearch()
}
