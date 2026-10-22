package com.railson.recipes.recipes.data.fake

import com.google.common.truth.Truth.assertThat
import com.railson.recipes.recipes.data.remote.PaginationDto
import com.railson.recipes.recipes.data.remote.RecipeDto
import org.junit.Test

class FakeRecipeSearchTest {

    @Test
    fun `catalog of 10 fits in page 1`() {
        val recipes = recipes(10)

        val result = FakeRecipeSearch.run(recipes, FakeSearchParams(page = 1, pageSize = 10))

        assertThat(result.data).isEqualTo(recipes)
        assertThat(result.pagination).isEqualTo(
            PaginationDto(
                page = 1, pageSize = 10, totalItems = 10, totalPages = 1,
                hasNext = false, nextPage = null, previousPage = null,
            ),
        )
    }

    @Test
    fun `catalog of 15 gives the first 10 and points to page 2`() {
        val recipes = recipes(15)

        val result = FakeRecipeSearch.run(recipes, FakeSearchParams(page = 1, pageSize = 10))

        assertThat(result.data).isEqualTo(recipes.take(10))
        assertThat(result.pagination).isEqualTo(
            PaginationDto(
                page = 1, pageSize = 10, totalItems = 15, totalPages = 2,
                hasNext = true, nextPage = 2, previousPage = null,
            ),
        )
    }
}

private fun recipes(count: Int) = (1..count).map { recipe("r$it") }

private fun recipe(id: String) = RecipeDto(
    id = id,
    title = "Recipe $id",
    description = "",
    servings = 2,
    prepTimeSeconds = 60,
    cookTimeSeconds = 60,
    difficulty = 1,
    cuisine = "",
    rating = null,
    imageUrl = null,
    dietaryAttributes = emptyList(),
    ingredients = emptyList(),
    instructions = emptyList(),
)
