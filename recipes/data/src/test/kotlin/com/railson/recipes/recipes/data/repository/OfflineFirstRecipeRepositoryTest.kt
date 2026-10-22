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
import com.railson.recipes.recipes.domain.model.SearchQuery
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
        repository = OfflineFirstRecipeRepository(api = api, dao = database.searchDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `nothing is saved before the first refresh`() = runTest {
        val result = repository.observeSearch(SearchQuery).first()

        assertThat(result.recipes).isEmpty()
    }

    @Test
    fun `refresh saves the catalog in the server's order`() = runTest {
        val outcome = repository.refresh(SearchQuery)

        val result = repository.observeSearch(SearchQuery).first()
        assertThat(outcome).isEqualTo(CallOutcome.Success)
        assertThat(result.recipes.map { it.title }).containsExactlyElementsIn(catalogTitles).inOrder()
        assertThat(result.hasMore).isFalse()
    }

    @Test
    fun `saved recipes keep their times and dietary tags`() = runTest {
        repository.refresh(SearchQuery)

        val recipes = repository.observeSearch(SearchQuery).first().recipes
        val curry = recipes.single { it.title == "Chickpea Coconut Curry" }
        assertThat(curry.prepTime).isEqualTo(10.minutes)
        assertThat(curry.cookTime).isEqualTo(20.minutes)
        assertThat(curry.dietaryTags).containsExactly(
            DietaryTag.Vegetarian, DietaryTag.Vegan, DietaryTag.GlutenFree, DietaryTag.DairyFree, DietaryTag.NutFree,
        )
    }

    @Test
    fun `refreshing again replaces the saved catalog instead of adding to it`() = runTest {
        repository.refresh(SearchQuery)

        repository.refresh(SearchQuery)

        assertThat(repository.observeSearch(SearchQuery).first().recipes).hasSize(catalogTitles.size)
    }

    @Test
    fun `slow still saves the catalog`() = runTest {
        networkConfig.mode = FakeNetworkMode.Slow

        assertThat(repository.refresh(SearchQuery)).isEqualTo(CallOutcome.Success)
        assertThat(repository.observeSearch(SearchQuery).first().recipes).hasSize(catalogTitles.size)
    }

    @Test
    fun `empty catalogue saves a catalog with no recipes`() = runTest {
        repository.refresh(SearchQuery)
        networkConfig.mode = FakeNetworkMode.EmptyCatalogue

        val outcome = repository.refresh(SearchQuery)

        assertThat(outcome).isEqualTo(CallOutcome.Success)
        assertThat(repository.observeSearch(SearchQuery).first().recipes).isEmpty()
    }

    @Test
    fun `each failing mode ends in its call outcome and keeps the saved catalog`() = runTest {
        repository.refresh(SearchQuery)
        val outcomes = failingModes.keys.associateWith { mode ->
            networkConfig.mode = mode
            repository.refresh(SearchQuery)
        }

        assertThat(outcomes).containsExactlyEntriesIn(failingModes)
        assertThat(repository.observeSearch(SearchQuery).first().recipes).hasSize(catalogTitles.size)
    }
}

private val catalogTitles = listOf(
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

private val failingModes = mapOf(
    FakeNetworkMode.NoNetwork to CallOutcome.NoConnection,
    FakeNetworkMode.Timeout to CallOutcome.Timeout,
    FakeNetworkMode.RateLimited to CallOutcome.RateLimited,
    FakeNetworkMode.ServerError to CallOutcome.ServerError,
    FakeNetworkMode.Unavailable to CallOutcome.Unavailable,
    FakeNetworkMode.BadData to CallOutcome.BadData,
)
