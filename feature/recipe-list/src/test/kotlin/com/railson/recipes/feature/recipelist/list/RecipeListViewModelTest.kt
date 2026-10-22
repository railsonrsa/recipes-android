package com.railson.recipes.feature.recipelist.list

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.railson.recipes.feature.recipelist.testing.FakeRecipeRepository
import com.railson.recipes.feature.recipelist.testing.MainDispatcherRule
import com.railson.recipes.recipes.domain.model.Recipe
import com.railson.recipes.recipes.domain.model.SearchQuery
import com.railson.recipes.recipes.domain.model.SearchResult
import com.railson.recipes.recipes.domain.usecase.ObserveSearchUseCase
import com.railson.recipes.recipes.domain.usecase.RefreshSearchUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.time.Duration

class RecipeListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeRecipeRepository()

    private fun viewModel() = RecipeListViewModel(
        observeSearch = ObserveSearchUseCase(repository),
        refreshSearch = RefreshSearchUseCase(repository),
    )

    @Test
    fun `opening the list refreshes the catalog`() {
        viewModel()

        assertThat(repository.refreshed).containsExactly(SearchQuery)
    }

    @Test
    fun `the list shows the saved recipes and follows their changes`() = runTest {
        val viewModel = viewModel()

        viewModel.uiState.test {
            assertThat(awaitItem().recipes).isEmpty()
            repository.saved.value = SearchResult(recipes = listOf(recipe("Greek Salad")), hasMore = false)
            assertThat(awaitItem().recipes.map { it.title }).containsExactly("Greek Salad")
        }
    }
}

private fun recipe(title: String) = Recipe(
    id = title,
    title = title,
    description = "",
    servings = 2,
    prepTime = Duration.ZERO,
    cookTime = Duration.ZERO,
    difficulty = null,
    cuisine = "",
    rating = null,
    imageUrl = null,
    dietaryTags = emptySet(),
    ingredients = emptyList(),
    instructions = emptyList(),
)
