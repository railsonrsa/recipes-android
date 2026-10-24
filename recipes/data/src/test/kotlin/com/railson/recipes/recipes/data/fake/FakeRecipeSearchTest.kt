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
    fun `page 2 of 15 gives the last 5 and no next page`() {
        val recipes = recipes(15)

        val result = FakeRecipeSearch.run(recipes, FakeSearchParams(page = 2, pageSize = 10))

        assertThat(result.data).isEqualTo(recipes.drop(10))
        assertThat(result.pagination).isEqualTo(
            PaginationDto(
                page = 2, pageSize = 10, totalItems = 15, totalPages = 2,
                hasNext = false, nextPage = null, previousPage = 1,
            ),
        )
    }

    @Test
    fun `a page past the end is empty`() {
        val result = FakeRecipeSearch.run(asset, FakeSearchParams(page = 3, pageSize = 10))

        assertThat(result.data).isEmpty()
        assertThat(result.pagination.totalItems).isEqualTo(20)
        assertThat(result.pagination.hasNext).isFalse()
        assertThat(result.pagination.nextPage).isNull()
    }

    @Test
    fun `a page past the end of a search is empty`() {
        val result = FakeRecipeSearch.run(asset, FakeSearchParams(page = 2, pageSize = 10, text = "lamb"))

        assertThat(result.data).isEmpty()
        assertThat(result.pagination.totalItems).isEqualTo(1)
        assertThat(result.pagination.hasNext).isFalse()
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
    fun `diet vegan finds the 4 vegan recipes`() {
        val result = FakeRecipeSearch.run(asset, params(diet = setOf("vegan")))

        assertThat(result.data.map { it.title })
            .containsExactly(CHICKPEA_CURRY, AVOCADO_MOUSSE, "Tomato Bruschetta", "Penne all'Arrabbiata")
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
    fun `diet vegetarian finds 11 recipes`() {
        val result = FakeRecipeSearch.run(asset, params(diet = setOf("vegetarian")))

        assertThat(result.pagination.totalItems).isEqualTo(11)
    }

    @Test
    fun `a recipe with no dietary tags matches no diet filter`() {
        val result = FakeRecipeSearch.run(asset, params(diet = setOf("nut-free")))

        assertThat(result.data.map { it.title }).containsNoneOf("Beef Tacos with Fresh Salsa", MARGHERITA_PIZZA)
        assertThat(result.pagination.totalItems).isEqualTo(18)
    }

    @Test
    fun `servings 1 to 2 finds 7 recipes`() {
        val result = FakeRecipeSearch.run(asset, params(minServings = 1, maxServings = 2))

        assertThat(result.data.map { it.title }).containsExactly(
            "Salmon Teriyaki Bowl",
            GREEK_SALAD,
            "Spaghetti with Cherry Tomatoes and Spinach",
            "Fluffy Buttermilk Pancakes",
            "Chicken Burrito",
            "Avocado Toast with Poached Egg",
            "Pan-Seared Steak with Peppercorn Sauce",
        )
    }

    @Test
    fun `servings 3 to 4 finds 9 recipes`() {
        val result = FakeRecipeSearch.run(asset, params(minServings = 3, maxServings = 4))

        assertThat(result.pagination.totalItems).isEqualTo(9)
    }

    @Test
    fun `servings 5 to 6 finds pao de queijo, the bruschetta and the butter chicken`() {
        val result = FakeRecipeSearch.run(asset, params(minServings = 5, maxServings = 6))

        assertThat(result.data.map { it.title })
            .containsExactly(PAO_DE_QUEIJO, "Tomato Bruschetta", "Butter Chicken with Naan")
    }

    @Test
    fun `servings 7 or more has no top and finds the lamb shoulder`() {
        val result = FakeRecipeSearch.run(asset, params(minServings = 7))

        assertThat(result.data.map { it.servings }).containsExactly(8)
    }

    @Test
    fun `difficulty easy finds 9 recipes`() {
        val result = FakeRecipeSearch.run(asset, params(difficulties = setOf(1)))

        assertThat(result.pagination.totalItems).isEqualTo(9)
    }

    @Test
    fun `difficulty medium or hard finds any of them`() {
        val result = FakeRecipeSearch.run(asset, params(difficulties = setOf(2, 3)))

        assertThat(result.pagination.totalItems).isEqualTo(11)
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
    fun `include garlic matches part of an ingredient name`() {
        val result = FakeRecipeSearch.run(asset, params(include = setOf("garlic")))

        assertThat(result.data.map { it.title }).containsExactly(CHICKPEA_CURRY, LEMON_HERB_CHICKEN)
    }

    @Test
    fun `exclude garlic finds the other 18`() {
        val result = FakeRecipeSearch.run(asset, params(exclude = setOf("garlic")))

        assertThat(result.pagination.totalItems).isEqualTo(18)
        assertThat(result.data.map { it.title }).containsNoneOf(CHICKPEA_CURRY, LEMON_HERB_CHICKEN)
    }

    @Test
    fun `include olive oil and exclude garlic find the pizza, the salad and the lamb`() {
        val result = FakeRecipeSearch.run(asset, params(include = setOf("olive oil"), exclude = setOf("garlic")))

        assertThat(result.data.map { it.title }).containsExactly(MARGHERITA_PIZZA, GREEK_SALAD, LAMB_SHOULDER)
    }

    @Test
    fun `every included word must match some ingredient`() {
        val result = FakeRecipeSearch.run(asset, params(include = setOf("garlic", "chickpeas")))

        assertThat(result.data.map { it.title }).containsExactly(CHICKPEA_CURRY)
    }

    @Test
    fun `exclude cheese keeps the tacos, whose cheddar has no cheese in its name`() {
        val result = FakeRecipeSearch.run(asset, params(exclude = setOf("cheese")))

        assertThat(result.pagination.totalItems).isEqualTo(18)
        assertThat(result.data.map { it.title }).contains("Beef Tacos with Fresh Salsa")
    }

    @Test
    fun `ingredient words ignore case and accents`() {
        val result = FakeRecipeSearch.run(asset, params(include = setOf("GARLÍC")))

        assertThat(result.data.map { it.title }).containsExactly(CHICKPEA_CURRY, LEMON_HERB_CHICKEN)
    }

    @Test
    fun `instructions oven finds the pizza and pao de queijo`() {
        val result = FakeRecipeSearch.run(asset, params(instructions = "oven"))

        assertThat(result.data.map { it.title }).containsExactly(MARGHERITA_PIZZA, PAO_DE_QUEIJO)
    }

    @Test
    fun `instructions saute finds the risotto's Saute step`() {
        val result = FakeRecipeSearch.run(asset, params(instructions = "saute"))

        assertThat(result.data.map { it.title }).containsExactly("Creamy Mushroom Risotto")
    }

    @Test
    fun `instructions oven and exclude cheese find the pizza`() {
        val result = FakeRecipeSearch.run(asset, params(instructions = "oven", exclude = setOf("cheese")))

        assertThat(result.data.map { it.title }).containsExactly(MARGHERITA_PIZZA)
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
private const val GREEK_SALAD = "Greek Salad"
private const val LAMB_SHOULDER = "Slow-Roasted Moroccan-Spiced Lamb Shoulder with Preserved Lemon, " +
    "Green Olives and Fresh Herb Couscous for a Big Family Sunday Gathering"
private const val LEMON_HERB_CHICKEN = "Grilled Lemon Herb Chicken"
private const val PAO_DE_QUEIJO = "Pão de Queijo (Brazilian Cheese Bread)"

/** docs/specs/test-data.md, "Filter counts". */
private val dietCounts = mapOf(
    "vegetarian" to 11,
    "vegan" to 4,
    "pescatarian" to 2,
    "halal" to 5,
    "gluten-free" to 9,
    "dairy-free" to 10,
    "nut-free" to 18,
    "low-carb" to 3,
)

private val difficultyCounts = mapOf(1 to 9, 2 to 7, 3 to 4)

@Suppress("LongParameterList") // One parameter per filter of the endpoint.
private fun params(
    diet: Set<String> = emptySet(),
    minServings: Int? = null,
    maxServings: Int? = null,
    difficulties: Set<Int> = emptySet(),
    include: Set<String> = emptySet(),
    exclude: Set<String> = emptySet(),
    instructions: String? = null,
) = FakeSearchParams(
    page = 1,
    pageSize = 10,
    diet = diet,
    minServings = minServings,
    maxServings = maxServings,
    difficulties = difficulties,
    include = include,
    exclude = exclude,
    instructions = instructions,
)

/** The 20 recipes the app ships with, in two assets. The cases come from docs/specs/test-data.md, "Searches". */
private val asset = listOf("recipes_page_1.json", "recipes_page_2.json").flatMap {
    createJson().decodeFromString<RecipePageDto>(File("src/main/assets/$it").readText()).data
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
