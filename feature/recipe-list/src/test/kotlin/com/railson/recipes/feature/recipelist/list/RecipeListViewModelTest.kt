package com.railson.recipes.feature.recipelist.list

import app.cash.turbine.test
import app.cash.turbine.turbineScope
import com.google.common.truth.Truth.assertThat
import com.railson.recipes.feature.recipelist.testing.FakeRecipeRepository
import com.railson.recipes.feature.recipelist.testing.MainDispatcherRule
import com.railson.recipes.recipes.domain.model.CallOutcome
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
    fun `nothing saved and the call running shows loading`() = runTest {
        val viewModel = viewModel()

        viewModel.uiState.test {
            assertThat(awaitItem()).isEqualTo(RecipeListUiState.Loading)
        }
    }

    @Test
    fun `nothing saved and no connection shows offline`() = runTest {
        val viewModel = viewModel()

        viewModel.uiState.test {
            skipItems(1)
            repository.endRefresh(CallOutcome.NoConnection)

            assertThat(awaitItem()).isEqualTo(RecipeListUiState.Offline)
        }
    }

    @Test
    fun `nothing saved and a timeout shows offline`() = runTest {
        val viewModel = viewModel()

        viewModel.uiState.test {
            skipItems(1)
            repository.endRefresh(CallOutcome.Timeout)

            assertThat(awaitItem()).isEqualTo(RecipeListUiState.Offline)
        }
    }

    @Test
    fun `nothing saved and a server error shows the error`() = runTest {
        val viewModel = viewModel()

        viewModel.uiState.test {
            skipItems(1)
            repository.endRefresh(CallOutcome.ServerError)

            assertThat(awaitItem()).isEqualTo(RecipeListUiState.Error)
        }
    }

    @Test
    fun `a saved catalog with no recipes shows the empty catalogue`() = runTest {
        val viewModel = viewModel()

        viewModel.uiState.test {
            skipItems(1)
            repository.saved.value = saved()
            repository.endRefresh(CallOutcome.Success)

            assertThat(expectMostRecentItem()).isEqualTo(RecipeListUiState.EmptyCatalogue)
        }
    }

    @Test
    fun `saved recipes and the call running show the list while it refreshes`() = runTest {
        repository.saved.value = saved(recipe("Greek Salad"))

        val viewModel = viewModel()

        viewModel.uiState.test {
            assertThat(expectMostRecentItem()).isEqualTo(
                RecipeListUiState.Content(recipes = listOf(recipe("Greek Salad")), isRefreshing = true),
            )
        }
    }

    @Test
    fun `the list stops refreshing when the call succeeds`() = runTest {
        repository.saved.value = saved(recipe("Greek Salad"))
        val viewModel = viewModel()

        viewModel.uiState.test {
            skipItems(1)
            repository.endRefresh(CallOutcome.Success)

            assertThat(expectMostRecentItem()).isEqualTo(
                RecipeListUiState.Content(recipes = listOf(recipe("Greek Salad")), isRefreshing = false),
            )
        }
    }

    @Test
    fun `saved recipes and no connection keep the list and say it is offline`() = runTest {
        repository.saved.value = saved(recipe("Greek Salad"))
        val viewModel = viewModel()

        turbineScope {
            val states = viewModel.uiState.testIn(backgroundScope)
            val effects = viewModel.effects.testIn(backgroundScope)

            repository.endRefresh(CallOutcome.NoConnection)

            assertThat(states.expectMostRecentItem()).isEqualTo(
                RecipeListUiState.Content(recipes = listOf(recipe("Greek Salad")), isRefreshing = false),
            )
            assertThat(effects.awaitItem()).isEqualTo(RecipeListEffect.ShowOfflineSnackbar)
        }
    }

    @Test
    fun `saved recipes and bad data keep the list and say the refresh failed`() = runTest {
        repository.saved.value = saved(recipe("Greek Salad"))
        val viewModel = viewModel()

        turbineScope {
            val states = viewModel.uiState.testIn(backgroundScope)
            val effects = viewModel.effects.testIn(backgroundScope)

            repository.endRefresh(CallOutcome.BadData)

            assertThat(states.expectMostRecentItem()).isEqualTo(
                RecipeListUiState.Content(recipes = listOf(recipe("Greek Salad")), isRefreshing = false),
            )
            assertThat(effects.awaitItem()).isEqualTo(RecipeListEffect.ShowRefreshFailedSnackbar)
        }
    }

    @Test
    fun `nothing saved and offline shows no snackbar`() = runTest {
        val viewModel = viewModel()

        viewModel.effects.test {
            repository.endRefresh(CallOutcome.NoConnection)

            expectNoEvents()
        }
    }

    @Test
    fun `refresh after an error calls the server again and shows loading`() = runTest {
        val viewModel = viewModel()

        viewModel.uiState.test {
            skipItems(1)
            repository.endRefresh(CallOutcome.ServerError)
            skipItems(1)

            viewModel.onAction(RecipeListAction.Refresh)

            assertThat(awaitItem()).isEqualTo(RecipeListUiState.Loading)
            assertThat(repository.refreshed).hasSize(2)
        }
    }

    @Test
    fun `refresh while a call runs does not start another`() {
        val viewModel = viewModel()

        viewModel.onAction(RecipeListAction.Refresh)

        assertThat(repository.refreshed).hasSize(1)
    }
}

private fun saved(vararg recipes: Recipe) = SearchResult(recipes = recipes.toList(), hasMore = false)

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
