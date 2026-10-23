package com.railson.recipes.feature.recipelist.list

import com.google.common.truth.Truth.assertThat
import com.railson.recipes.feature.recipelist.testing.FakeRecipeRepository
import com.railson.recipes.feature.recipelist.testing.MainDispatcherRule
import com.railson.recipes.recipes.domain.model.CallOutcome
import com.railson.recipes.recipes.domain.model.Recipe
import com.railson.recipes.recipes.domain.model.SearchQuery
import com.railson.recipes.recipes.domain.model.SearchResult
import com.railson.recipes.recipes.domain.usecase.CountRecipesUseCase
import com.railson.recipes.recipes.domain.usecase.LoadNextPageUseCase
import com.railson.recipes.recipes.domain.usecase.ObserveSearchUseCase
import com.railson.recipes.recipes.domain.usecase.RefreshSearchUseCase
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.time.Duration

class RecipeListViewModelPagingTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeRecipeRepository()

    private fun viewModel() = RecipeListViewModel(
        observeSearch = ObserveSearchUseCase(repository),
        refreshSearch = RefreshSearchUseCase(repository),
        loadNextPage = LoadNextPageUseCase(repository),
        countRecipes = CountRecipesUseCase(repository),
    )

    /** Keeps [RecipeListViewModel.uiState] collected, so its value follows the ViewModel. */
    private fun TestScope.uiStateOf(viewModel: RecipeListViewModel): StateFlow<RecipeListUiState> {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
        return viewModel.uiState
    }

    /** The catalog's first page of 10 out of 15, saved and refreshed. */
    private fun TestScope.firstPageShown(): Pair<RecipeListViewModel, StateFlow<RecipeListUiState>> {
        repository.saved.value = page(size = 10, totalItems = 15, hasMore = true)
        val viewModel = viewModel()
        val uiState = uiStateOf(viewModel)
        repository.endRefresh(CallOutcome.Success)
        return viewModel to uiState
    }

    @Test
    fun `the list knows the server's total, not only the loaded recipes`() = runTest {
        val (_, uiState) = firstPageShown()

        assertThat(uiState.content.recipes).hasSize(10)
        assertThat(uiState.content.totalItems).isEqualTo(15)
    }

    @Test
    fun `a search with more recipes ends with the loading more row`() = runTest {
        val (_, uiState) = firstPageShown()

        assertThat(uiState.content.listEnd).isEqualTo(ListEnd.LoadingMore)
    }

    @Test
    fun `a search with everything loaded ends with that's all`() = runTest {
        repository.saved.value = page(size = 10, totalItems = 10, hasMore = false)
        val uiState = uiStateOf(viewModel())
        repository.endRefresh(CallOutcome.Success)

        assertThat(uiState.content.listEnd).isEqualTo(ListEnd.AllLoaded)
    }

    @Test
    fun `load next page asks for the next page of the current search`() = runTest {
        val (viewModel, _) = firstPageShown()

        viewModel.onAction(RecipeListAction.LoadNextPage)

        assertThat(repository.nextPagesLoaded).containsExactly(SearchQuery.Catalog)
    }

    @Test
    fun `load next page while one runs does not start another`() = runTest {
        val (viewModel, _) = firstPageShown()

        viewModel.onAction(RecipeListAction.LoadNextPage)
        viewModel.onAction(RecipeListAction.LoadNextPage)

        assertThat(repository.nextPagesLoaded).hasSize(1)
    }

    @Test
    fun `load next page waits for the refresh`() = runTest {
        repository.saved.value = page(size = 10, totalItems = 15, hasMore = true)
        val viewModel = viewModel()

        viewModel.onAction(RecipeListAction.LoadNextPage)

        assertThat(repository.nextPagesLoaded).isEmpty()
    }

    @Test
    fun `load next page with everything loaded makes no call`() = runTest {
        repository.saved.value = page(size = 10, totalItems = 10, hasMore = false)
        val viewModel = viewModel()
        repository.endRefresh(CallOutcome.Success)

        viewModel.onAction(RecipeListAction.LoadNextPage)

        assertThat(repository.nextPagesLoaded).isEmpty()
    }

    @Test
    fun `a failed next page says couldn't load more and keeps the cards`() = runTest {
        val (viewModel, uiState) = firstPageShown()

        viewModel.onAction(RecipeListAction.LoadNextPage)
        repository.endNextPage(CallOutcome.NoConnection)

        assertThat(uiState.content.listEnd).isEqualTo(ListEnd.LoadMoreFailed)
        assertThat(uiState.content.recipes).hasSize(10)
    }

    @Test
    fun `retry after a failed next page asks again and shows loading more`() = runTest {
        val (viewModel, uiState) = firstPageShown()
        viewModel.onAction(RecipeListAction.LoadNextPage)
        repository.endNextPage(CallOutcome.ServerError)

        viewModel.onAction(RecipeListAction.LoadNextPage)

        assertThat(repository.nextPagesLoaded).hasSize(2)
        assertThat(uiState.content.listEnd).isEqualTo(ListEnd.LoadingMore)
    }

    @Test
    fun `refresh after a failed next page shows loading more again`() = runTest {
        val (viewModel, uiState) = firstPageShown()
        viewModel.onAction(RecipeListAction.LoadNextPage)
        repository.endNextPage(CallOutcome.ServerError)

        viewModel.onAction(RecipeListAction.Refresh)

        assertThat(uiState.content.listEnd).isEqualTo(ListEnd.LoadingMore)
    }

    @Test
    fun `a new search does not show the old search's failed next page`() = runTest {
        val (viewModel, uiState) = firstPageShown()
        viewModel.onAction(RecipeListAction.LoadNextPage)
        repository.endNextPage(CallOutcome.ServerError)
        repository.savedFor(SearchQuery(text = "cheese")).value = page(size = 10, totalItems = 12, hasMore = true)

        viewModel.onAction(RecipeListAction.ChangeSearchText("cheese"))
        advanceTimeBy(SEARCH_TEXT_WAIT_PLUS_ONE)

        assertThat(uiState.content.listEnd).isEqualTo(ListEnd.LoadingMore)
    }
}

private val StateFlow<RecipeListUiState>.content: RecipeListUiState.Content
    get() = value as RecipeListUiState.Content

private val SEARCH_TEXT_WAIT_PLUS_ONE = Duration.parse("301ms")

private fun page(size: Int, totalItems: Int, hasMore: Boolean) = SearchResult(
    recipes = List(size) { recipe("recipe-$it") },
    hasMore = hasMore,
    totalItems = totalItems,
)

private fun recipe(id: String) = Recipe(
    id = id,
    title = id,
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
