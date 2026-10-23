package com.railson.recipes.recipes.data.fake

import com.google.common.truth.Truth.assertThat
import com.railson.recipes.core.network.client.createJson
import com.railson.recipes.recipes.data.remote.PaginationDto
import com.railson.recipes.recipes.data.remote.RecipeDto
import com.railson.recipes.recipes.data.remote.RecipePageDto
import org.junit.Test
import java.io.File

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

    @Test
    fun `text "pao" finds Pao de Queijo`() {
        val result = FakeRecipeSearch.run(asset, FakeSearchParams(page = 1, pageSize = 10, text = "pao"))

        assertThat(result.data.map { it.title }).containsExactly("Pão de Queijo (Brazilian Cheese Bread)")
    }

    @Test
    fun `text "vegetalien" finds the avocado mousse by its title`() {
        val result = FakeRecipeSearch.run(asset, FakeSearchParams(page = 1, pageSize = 10, text = "vegetalien"))

        assertThat(result.data.map { it.title }).containsExactly(AVOCADO_MOUSSE)
    }

    @Test
    fun `text "CREME" finds the avocado mousse by its description`() {
        val result = FakeRecipeSearch.run(asset, FakeSearchParams(page = 1, pageSize = 10, text = "CREME"))

        assertThat(result.data.map { it.title }).containsExactly(AVOCADO_MOUSSE)
    }

    @Test
    fun `text no recipe has gives an empty page 1`() {
        val result = FakeRecipeSearch.run(asset, FakeSearchParams(page = 1, pageSize = 10, text = "sushi"))

        assertThat(result.data).isEmpty()
        assertThat(result.pagination.totalItems).isEqualTo(0)
        assertThat(result.pagination.hasNext).isFalse()
    }

    @Test
    fun `pagination counts only the recipes that match`() {
        val lamb = listOf(recipe("lamb-1").copy(title = "Lamb stew"), recipe("lamb-2").copy(title = "Lamb pie"))
        val recipes = recipes(15) + lamb

        val result = FakeRecipeSearch.run(recipes, FakeSearchParams(page = 1, pageSize = 10, text = "lamb"))

        assertThat(result.data.map { it.id }).containsExactly("lamb-1", "lamb-2").inOrder()
        assertThat(result.pagination).isEqualTo(
            PaginationDto(
                page = 1, pageSize = 10, totalItems = 2, totalPages = 1,
                hasNext = false, nextPage = null, previousPage = null,
            ),
        )
    }
}

private const val AVOCADO_MOUSSE = "Mousse au Chocolat Végétalien à l'Avocat 🥑🍫"

/** The 10 recipes the app ships with. The cases come from docs/specs/test-data.md, "Searches". */
private val asset = createJson()
    .decodeFromString<RecipePageDto>(File("src/main/assets/recipes_page_1.json").readText())
    .data

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
