package com.railson.recipes.recipes.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.railson.recipes.core.network.client.createJson
import com.railson.recipes.core.network.client.createOkHttpClient
import com.railson.recipes.core.network.client.createRetrofit
import com.railson.recipes.core.network.fake.FakeNetworkConfig
import com.railson.recipes.core.network.fake.FakeNetworkMode
import com.railson.recipes.core.network.fake.FakeNetworkModeInterceptor
import com.railson.recipes.recipes.data.di.createRecipesApi
import com.railson.recipes.recipes.data.local.database.RecipesDatabase
import com.railson.recipes.recipes.domain.model.CallOutcome
import com.railson.recipes.recipes.domain.model.DietaryTag
import com.railson.recipes.recipes.domain.model.Difficulty
import com.railson.recipes.recipes.domain.model.SearchQuery
import com.railson.recipes.recipes.domain.model.ServingsRange
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.time.Duration.Companion.minutes

@RunWith(RobolectricTestRunner::class)
class OfflineFirstRecipeRepositoryTest {

    private lateinit var database: RecipesDatabase
    private val networkConfig = FakeNetworkConfig()
    private lateinit var repository: OfflineFirstRecipeRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, RecipesDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val json = createJson()
        val client = createOkHttpClient(logBodies = true, FakeNetworkModeInterceptor(networkConfig, wait = {}))
        val api = createRecipesApi(context, client, createRetrofit(client, json), json, networkConfig)
        repository = OfflineFirstRecipeRepository(
            api = api,
            recipeSearchDao = database.recipeSearchDao(),
            recipeDao = database.recipeDao(),
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `nothing is saved before the first refresh`() = runTest {
        val result = repository.observeSearch(SearchQuery.Catalog).first()

        assertThat(result).isNull()
    }

    @Test
    fun `refresh saves page 1 of the catalog in the server's order`() = runTest {
        val outcome = repository.refresh(SearchQuery.Catalog)

        val result = repository.observeSearch(SearchQuery.Catalog).first()
        assertThat(outcome).isEqualTo(CallOutcome.Success)
        assertThat(result?.recipes?.map { it.title }).containsExactlyElementsIn(page1Titles).inOrder()
        assertThat(result?.hasMore).isTrue()
    }

    @Test
    fun `saved recipes keep their times and dietary tags`() = runTest {
        repository.refresh(SearchQuery.Catalog)

        val recipes = repository.observeSearch(SearchQuery.Catalog).first()?.recipes.orEmpty()
        val curry = recipes.single { it.title == "Chickpea Coconut Curry" }
        assertThat(curry.prepTime).isEqualTo(10.minutes)
        assertThat(curry.cookTime).isEqualTo(20.minutes)
        assertThat(curry.dietaryTags).containsExactly(
            DietaryTag.Vegetarian, DietaryTag.Vegan, DietaryTag.GlutenFree, DietaryTag.DairyFree, DietaryTag.NutFree,
        )
    }

    @Test
    fun `refreshing again replaces the saved catalog instead of adding to it`() = runTest {
        repository.refresh(SearchQuery.Catalog)

        repository.refresh(SearchQuery.Catalog)

        assertThat(repository.observeSearch(SearchQuery.Catalog).first()?.recipes).hasSize(page1Titles.size)
    }

    @Test
    fun `slow still saves the catalog`() = runTest {
        networkConfig.mode = FakeNetworkMode.Slow

        assertThat(repository.refresh(SearchQuery.Catalog)).isEqualTo(CallOutcome.Success)
        assertThat(repository.observeSearch(SearchQuery.Catalog).first()?.recipes).hasSize(page1Titles.size)
    }

    @Test
    fun `empty catalogue saves a catalog with no recipes`() = runTest {
        repository.refresh(SearchQuery.Catalog)
        networkConfig.mode = FakeNetworkMode.EmptyCatalogue

        val outcome = repository.refresh(SearchQuery.Catalog)

        assertThat(outcome).isEqualTo(CallOutcome.Success)
        assertThat(repository.observeSearch(SearchQuery.Catalog).first()?.recipes).isEmpty()
    }

    @Test
    fun `each failing mode ends in its call outcome and keeps the saved catalog`() = runTest {
        repository.refresh(SearchQuery.Catalog)
        val outcomes = failingModes.keys.associateWith { mode ->
            networkConfig.mode = mode
            repository.refresh(SearchQuery.Catalog)
        }

        assertThat(outcomes).containsExactlyEntriesIn(failingModes)
        assertThat(repository.observeSearch(SearchQuery.Catalog).first()?.recipes).hasSize(page1Titles.size)
    }

    @Test
    fun `two searches keep separate results`() = runTest {
        repository.refresh(SearchQuery.Catalog)

        repository.refresh(SearchQuery(text = "pao"))

        assertThat(repository.observeSearch(SearchQuery(text = "pao")).first()?.recipes?.map { it.title })
            .containsExactly("Pão de Queijo (Brazilian Cheese Bread)")
        assertThat(repository.observeSearch(SearchQuery.Catalog).first()?.recipes).hasSize(page1Titles.size)
    }

    @Test
    fun `a saved search shows with no network`() = runTest {
        repository.refresh(SearchQuery(text = "pao"))
        networkConfig.mode = FakeNetworkMode.NoNetwork

        val outcome = repository.refresh(SearchQuery(text = "Pão"))

        assertThat(outcome).isEqualTo(CallOutcome.NoConnection)
        assertThat(repository.observeSearch(SearchQuery(text = "Pão")).first()?.recipes?.map { it.title })
            .containsExactly("Pão de Queijo (Brazilian Cheese Bread)")
    }

    @Test
    fun `refresh with filters saves only the recipes the server matched`() = runTest {
        val query = SearchQuery(
            dietaryTags = setOf(DietaryTag.Vegetarian),
            servingsRange = ServingsRange.ThreeToFour,
            difficulties = setOf(Difficulty.Easy),
        )

        repository.refresh(query)

        assertThat(repository.observeSearch(query).first()?.recipes?.map { it.title })
            .containsExactly("Chickpea Coconut Curry", "Mousse au Chocolat Végétalien à l'Avocat 🥑🍫")
    }

    @Test
    fun `refresh with ingredients and an instruction search saves what the server matched`() = runTest {
        val query = SearchQuery(
            includedIngredients = setOf("Olive oil"),
            excludedIngredients = setOf("garlic"),
            instructionSearch = "Oven",
        )

        repository.refresh(query)

        assertThat(repository.observeSearch(query).first()?.recipes?.map { it.title })
            .containsExactly("Classic Margherita Pizza")
    }

    @Test
    fun `vegan and halal saves a search with no recipes`() = runTest {
        val query = SearchQuery(dietaryTags = setOf(DietaryTag.Vegan, DietaryTag.Halal))

        val outcome = repository.refresh(query)

        assertThat(outcome).isEqualTo(CallOutcome.Success)
        assertThat(repository.observeSearch(query).first()?.recipes).isEmpty()
    }

    @Test
    fun `the saved result has the server's total, not only the loaded recipes`() = runTest {
        repository.refresh(SearchQuery.Catalog)

        val result = repository.observeSearch(SearchQuery.Catalog).first()
        assertThat(result?.recipes?.map { it.title }).containsExactlyElementsIn(page1Titles).inOrder()
        assertThat(result?.totalItems).isEqualTo(20)
        assertThat(result?.hasMore).isTrue()
    }

    @Test
    fun `next page appends page 2, the last one of the catalog`() = runTest {
        repository.refresh(SearchQuery.Catalog)

        val outcome = repository.loadNextPage(SearchQuery.Catalog)

        val result = repository.observeSearch(SearchQuery.Catalog).first()
        assertThat(outcome).isEqualTo(CallOutcome.Success)
        assertThat(result?.recipes?.map { it.title }).containsExactlyElementsIn(page1Titles + page2Titles).inOrder()
        assertThat(result?.hasMore).isFalse()
        assertThat(result?.totalItems).isEqualTo(20)
    }

    @Test
    fun `next page of a search with nothing more makes no call`() = runTest {
        repository.refresh(SearchQuery.Catalog)
        repository.loadNextPage(SearchQuery.Catalog)
        // A call would fail, so a success proves none was made.
        networkConfig.mode = FakeNetworkMode.NoNetwork

        val outcome = repository.loadNextPage(SearchQuery.Catalog)

        assertThat(outcome).isEqualTo(CallOutcome.Success)
        assertThat(repository.observeSearch(SearchQuery.Catalog).first()?.recipes).hasSize(20)
    }

    @Test
    fun `next page of a search never saved makes no call`() = runTest {
        networkConfig.mode = FakeNetworkMode.NoNetwork

        val outcome = repository.loadNextPage(SearchQuery.Catalog)

        assertThat(outcome).isEqualTo(CallOutcome.Success)
        assertThat(repository.observeSearch(SearchQuery.Catalog).first()).isNull()
    }

    @Test
    fun `a failed next page keeps the saved recipes`() = runTest {
        repository.refresh(SearchQuery.Catalog)
        networkConfig.mode = FakeNetworkMode.ServerError

        val outcome = repository.loadNextPage(SearchQuery.Catalog)

        val result = repository.observeSearch(SearchQuery.Catalog).first()
        assertThat(outcome).isEqualTo(CallOutcome.ServerError)
        assertThat(result?.recipes).hasSize(10)
        assertThat(result?.hasMore).isTrue()
    }

    @Test
    fun `next page after a failed one asks for the same page again`() = runTest {
        repository.refresh(SearchQuery.Catalog)
        networkConfig.mode = FakeNetworkMode.ServerError
        repository.loadNextPage(SearchQuery.Catalog)
        networkConfig.mode = FakeNetworkMode.Normal

        repository.loadNextPage(SearchQuery.Catalog)

        assertThat(repository.observeSearch(SearchQuery.Catalog).first()?.recipes?.map { it.title })
            .containsExactlyElementsIn(page1Titles + page2Titles).inOrder()
    }

    @Test
    fun `refresh after a next page starts again from page 1`() = runTest {
        repository.refresh(SearchQuery.Catalog)
        repository.loadNextPage(SearchQuery.Catalog)

        repository.refresh(SearchQuery.Catalog)

        val result = repository.observeSearch(SearchQuery.Catalog).first()
        assertThat(result?.recipes?.map { it.title }).containsExactlyElementsIn(page1Titles).inOrder()
        assertThat(result?.hasMore).isTrue()
    }

    @Test
    fun `next page of one search leaves the other searches alone`() = runTest {
        repository.refresh(SearchQuery.Catalog)
        repository.refresh(SearchQuery(text = "cheese"))

        repository.loadNextPage(SearchQuery.Catalog)

        assertThat(repository.observeSearch(SearchQuery(text = "cheese")).first()?.recipes).hasSize(2)
    }

    @Test
    fun `count reads the server total for the text and the filters together`() = runTest {
        val count = repository.count(SearchQuery(text = "cheese", dietaryTags = setOf(DietaryTag.GlutenFree)))

        // "cheese" alone finds 2, gluten-free alone 6: only Pão de Queijo has both.
        assertThat(count).isEqualTo(1)
    }

    @Test
    fun `count saves nothing`() = runTest {
        val query = SearchQuery(dietaryTags = setOf(DietaryTag.Vegan))

        repository.count(query)

        assertThat(repository.observeSearch(query).first()).isNull()
    }

    @Test
    fun `count is null when the call fails`() = runTest {
        networkConfig.mode = FakeNetworkMode.NoNetwork

        assertThat(repository.count(SearchQuery(dietaryTags = setOf(DietaryTag.Vegan)))).isNull()
    }

    @Test
    fun `a search never made is not saved even when its recipes are`() = runTest {
        repository.refresh(SearchQuery.Catalog)

        val result = repository.observeSearch(SearchQuery(text = "pao")).first()

        assertThat(result).isNull()
    }

    @Test
    fun `a saved recipe is read by its id with no network`() = runTest {
        repository.refresh(SearchQuery.Catalog)
        val curry = repository.observeSearch(SearchQuery.Catalog).first()?.recipes.orEmpty()
            .single { it.title == "Chickpea Coconut Curry" }
        networkConfig.mode = FakeNetworkMode.NoNetwork

        val recipe = repository.observeRecipe(curry.id).first()

        assertThat(recipe).isEqualTo(curry)
    }

    @Test
    fun `a recipe that was never saved reads as null`() = runTest {
        repository.refresh(SearchQuery.Catalog)

        val recipe = repository.observeRecipe("never-saved").first()

        assertThat(recipe).isNull()
    }
}

private val page1Titles = listOf(
    "Classic Margherita Pizza",
    "Chickpea Coconut Curry",
    "Grilled Lemon Herb Chicken",
    "Creamy Mushroom Risotto",
    "Beef Tacos with Fresh Salsa",
    "Salmon Teriyaki Bowl",
    "Mousse au Chocolat Végétalien à l'Avocat 🥑🍫",
    "Greek Salad",
    "Pão de Queijo (Brazilian Cheese Bread)",
    "Slow-Roasted Moroccan-Spiced Lamb Shoulder with Preserved Lemon, Green Olives and Fresh Herb Couscous " +
        "for a Big Family Sunday Gathering",
)

private val page2Titles = listOf(
    "Tomato Bruschetta",
    "Spaghetti with Cherry Tomatoes and Spinach",
    "Penne all'Arrabbiata",
    "Spicy Chicken Skewers with Rice",
    "Fluffy Buttermilk Pancakes",
    "Chicken Burrito",
    "Thai Shrimp Noodle Soup",
    "Avocado Toast with Poached Egg",
    "Butter Chicken with Naan",
    "Pan-Seared Steak with Peppercorn Sauce",
)

private val failingModes = mapOf(
    FakeNetworkMode.NoNetwork to CallOutcome.NoConnection,
    FakeNetworkMode.Timeout to CallOutcome.Timeout,
    FakeNetworkMode.RateLimited to CallOutcome.RateLimited,
    FakeNetworkMode.ServerError to CallOutcome.ServerError,
    FakeNetworkMode.Unavailable to CallOutcome.Unavailable,
    FakeNetworkMode.BadData to CallOutcome.BadData,
)
