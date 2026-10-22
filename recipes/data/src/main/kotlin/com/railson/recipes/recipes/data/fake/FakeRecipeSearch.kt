package com.railson.recipes.recipes.data.fake

import com.railson.recipes.recipes.data.remote.PaginationDto
import com.railson.recipes.recipes.data.remote.RecipeDto
import com.railson.recipes.recipes.data.remote.RecipePageDto

/** What a call to `GET /recipes` asked for. Filters come with search (issues #23 and #24). */
internal data class FakeSearchParams(
    val page: Int,
    val pageSize: Int,
)

/** The fake server's answer to `GET /recipes`, without any HTTP. */
internal object FakeRecipeSearch {
    fun run(recipes: List<RecipeDto>, params: FakeSearchParams): RecipePageDto {
        val (page, pageSize) = params
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
