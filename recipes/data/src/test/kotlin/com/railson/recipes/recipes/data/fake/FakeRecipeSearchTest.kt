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
    fun `diet vegan finds the 2 vegan recipes`() {
        val result = FakeRecipeSearch.run(asset, params(diet = setOf("vegan")))

        assertThat(result.data.map { it.title }).containsExactly(CHICKPEA_CURRY, AVOCADO_MOUSSE)
    }

    @Test
    fun `diet vegan and gluten-free needs both tags`() {
        val result = FakeRecipeSearch.run(asset, params(diet = setOf("vegan", "gluten-free")))

        assertThat(result.data.map { it.title }).containsExactly(CHICKPEA_CURRY, AVOCADO_MOUSSE)
    }

    @Test
    fun `diet vegan and halal finds nothing`() {
        val result = FakeRecipeSearch.run(asset, params(diet = setOf("vegan", "halal")))

        assertThat(result.data).isEmpty()
        assertThat(result.pagination.totalItems).isEqualTo(0)
    }

    @Test
    fun `diet vegetarian finds 6 recipes`() {
        val result = FakeRecipeSearch.run(asset, params(diet = setOf("vegetarian")))

        assertThat(result.pagination.totalItems).isEqualTo(6)
    }

    @Test
    fun `a recipe with no dietary tags matches no diet filter`() {
        val result = FakeRecipeSearch.run(asset, params(diet = setOf("nut-free")))

        assertThat(result.data.map { it.title }).containsNoneOf("Beef Tacos with Fresh Salsa", MARGHERITA_PIZZA)
        assertThat(result.pagination.totalItems).isEqualTo(8)
    }

    @Test
    fun `servings 1 to 2 finds the salmon bowl and the greek salad`() {
        val result = FakeRecipeSearch.run(asset, params(minServings = 1, maxServings = 2))

        assertThat(result.data.map { it.title }).containsExactly("Salmon Teriyaki Bowl", "Greek Salad")
    }

    @Test
    fun `servings 3 to 4 finds 6 recipes`() {
        val result = FakeRecipeSearch.run(asset, params(minServings = 3, maxServings = 4))

        assertThat(result.pagination.totalItems).isEqualTo(6)
    }

    @Test
    fun `servings 5 to 6 finds pao de queijo`() {
        val result = FakeRecipeSearch.run(asset, params(minServings = 5, maxServings = 6))

        assertThat(result.data.map { it.title }).containsExactly(PAO_DE_QUEIJO)
    }

    @Test
    fun `servings 7 or more has no top and finds the lamb shoulder`() {
        val result = FakeRecipeSearch.run(asset, params(minServings = 7))

        assertThat(result.data.map { it.servings }).containsExactly(8)
    }

    @Test
    fun `difficulty easy finds 5 recipes`() {
        val result = FakeRecipeSearch.run(asset, params(difficulties = setOf(1)))

        assertThat(result.pagination.totalItems).isEqualTo(5)
    }

    @Test
    fun `difficulty medium or hard finds any of them`() {
        val result = FakeRecipeSearch.run(asset, params(difficulties = setOf(2, 3)))

        assertThat(result.pagination.totalItems).isEqualTo(5)
    }

    @Test
    fun `a recipe with no difficulty never matches a difficulty filter`() {
        val unknown = recipe("unknown").copy(difficulty = null)

        val result = FakeRecipeSearch.run(listOf(unknown), params(difficulties = setOf(1, 2, 3)))

        assertThat(result.data).isEmpty()
    }

    @Test
    fun `diet vegetarian, servings 3 to 4 and easy find the curry and the mousse`() {
        val result = FakeRecipeSearch.run(
            asset,
            params(diet = setOf("vegetarian"), minServings = 3, maxServings = 4, difficulties = setOf(1)),
        )

        assertThat(result.data.map { it.title }).containsExactly(CHICKPEA_CURRY, AVOCADO_MOUSSE)
    }

    @Test
    fun `each diet alone gives its count from the test data`() {
        val counts = dietCounts.keys.associateWith { key ->
            FakeRecipeSearch.run(asset, params(diet = setOf(key))).pagination.totalItems
        }

        assertThat(counts).containsExactlyEntriesIn(dietCounts)
    }

    @Test
    fun `each difficulty alone gives its count from the test data`() {
        val counts = difficultyCounts.keys.associateWith { number ->
            FakeRecipeSearch.run(asset, params(difficulties = setOf(number))).pagination.totalItems
        }

        assertThat(counts).containsExactlyEntriesIn(difficultyCounts)
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
private const val CHICKPEA_CURRY = "Chickpea Coconut Curry"
private const val MARGHERITA_PIZZA = "Classic Margherita Pizza"
private const val PAO_DE_QUEIJO = "Pão de Queijo (Brazilian Cheese Bread)"

/** docs/specs/test-data.md, "Filter counts". */
private val dietCounts = mapOf(
    "vegetarian" to 6,
    "vegan" to 2,
    "pescatarian" to 1,
    "halal" to 2,
    "gluten-free" to 6,
    "dairy-free" to 5,
    "nut-free" to 8,
    "low-carb" to 2,
)

private val difficultyCounts = mapOf(1 to 5, 2 to 3, 3 to 2)

private fun params(
    diet: Set<String> = emptySet(),
    minServings: Int? = null,
    maxServings: Int? = null,
    difficulties: Set<Int> = emptySet(),
) = FakeSearchParams(
    page = 1,
    pageSize = 10,
    diet = diet,
    minServings = minServings,
    maxServings = maxServings,
    difficulties = difficulties,
)

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
