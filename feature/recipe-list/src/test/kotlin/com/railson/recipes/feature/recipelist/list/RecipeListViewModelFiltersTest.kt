package com.railson.recipes.feature.recipelist.list

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.railson.recipes.feature.recipelist.filter.ActiveFilter
import com.railson.recipes.feature.recipelist.filter.RecipeFiltersUiState
import com.railson.recipes.feature.recipelist.testing.FakeRecipeRepository
import com.railson.recipes.feature.recipelist.testing.MainDispatcherRule
import com.railson.recipes.recipes.domain.model.CallOutcome
import com.railson.recipes.recipes.domain.model.DietaryTag
import com.railson.recipes.recipes.domain.model.Difficulty
import com.railson.recipes.recipes.domain.model.SearchQuery
import com.railson.recipes.recipes.domain.model.SearchResult
import com.railson.recipes.recipes.domain.model.ServingsRange
import com.railson.recipes.recipes.domain.usecase.CountRecipesUseCase
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
import kotlin.time.Duration.Companion.seconds

class RecipeListViewModelFiltersTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeRecipeRepository()

    private fun viewModel() = RecipeListViewModel(
        observeSearch = ObserveSearchUseCase(repository),
        refreshSearch = RefreshSearchUseCase(repository),
        countRecipes = CountRecipesUseCase(repository),
    )

    /** Keeps [RecipeListViewModel.filters] collected, so its value follows the ViewModel. */
    private fun TestScope.filtersOf(viewModel: RecipeListViewModel): StateFlow<RecipeFiltersUiState> {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.filters.collect {} }
        return viewModel.filters
    }

    @Test
    fun `opening the filters counts the applied search right away`() = runTest {
        repository.counts[SearchQuery.Catalog] = 10
        val viewModel = viewModel()
        val filters = filtersOf(viewModel)

        viewModel.onAction(RecipeListAction.OpenFilters)

        assertThat(filters.value.draftCount).isEqualTo(10)
    }

    @Test
    fun `a filter change counts the draft 300 ms after the last change`() = runTest {
        val vegan = SearchQuery(dietaryTags = setOf(DietaryTag.Vegan))
        repository.counts[vegan] = 2
        val viewModel = viewModel()
        val filters = filtersOf(viewModel)
        viewModel.onAction(RecipeListAction.OpenFilters)

        viewModel.onAction(RecipeListAction.ToggleDietaryTag(DietaryTag.Vegan))
        advanceTimeBy(299)

        assertThat(repository.counted).containsExactly(SearchQuery.Catalog)
        advanceTimeBy(2)
        assertThat(repository.counted).containsExactly(SearchQuery.Catalog, vegan).inOrder()
        assertThat(filters.value.draftCount).isEqualTo(2)
    }

    @Test
    fun `changes within 300 ms count only the last draft`() = runTest {
        val viewModel = viewModel()
        viewModel.onAction(RecipeListAction.OpenFilters)

        viewModel.onAction(RecipeListAction.ToggleDietaryTag(DietaryTag.Vegan))
        advanceTimeBy(200)
        viewModel.onAction(RecipeListAction.ChangeServingsRange(ServingsRange.ThreeToFour))
        advanceTimeBy(200)
        viewModel.onAction(RecipeListAction.ToggleDifficulty(Difficulty.Easy))
        advanceTimeBy(301)

        assertThat(repository.counted).containsExactly(
            SearchQuery.Catalog,
            SearchQuery(
                dietaryTags = setOf(DietaryTag.Vegan),
                servingsRange = ServingsRange.ThreeToFour,
                difficulties = setOf(Difficulty.Easy),
            ),
        ).inOrder()
    }

    @Test
    fun `a failed count drops the number the button showed`() = runTest {
        repository.counts[SearchQuery.Catalog] = 10
        val viewModel = viewModel()
        val filters = filtersOf(viewModel)
        viewModel.onAction(RecipeListAction.OpenFilters)
        assertThat(filters.value.draftCount).isEqualTo(10)

        viewModel.onAction(RecipeListAction.ToggleDietaryTag(DietaryTag.Halal))
        advanceTimeBy(301)

        assertThat(repository.counted.last()).isEqualTo(SearchQuery(dietaryTags = setOf(DietaryTag.Halal)))
        assertThat(filters.value.draftCount).isNull()
    }

    @Test
    fun `reopening the sheet keeps the last number while the new count loads`() = runTest {
        repository.counts[SearchQuery.Catalog] = 10
        val viewModel = viewModel()
        val filters = filtersOf(viewModel)
        viewModel.onAction(RecipeListAction.OpenFilters)
        repository.countWait = 1.seconds

        viewModel.onAction(RecipeListAction.OpenFilters)

        assertThat(filters.value.draftCount).isEqualTo(10)
    }

    @Test
    fun `the count includes the search text`() = runTest {
        val viewModel = viewModel()
        viewModel.onAction(RecipeListAction.ChangeSearchText("cheese"))
        advanceTimeBy(301)

        viewModel.onAction(RecipeListAction.OpenFilters)

        assertThat(repository.counted).containsExactly(SearchQuery(text = "cheese"))
    }

    @Test
    fun `editing the draft does not change the search`() = runTest {
        val viewModel = viewModel()
        val filters = filtersOf(viewModel)
        viewModel.onAction(RecipeListAction.OpenFilters)

        viewModel.onAction(RecipeListAction.ToggleDietaryTag(DietaryTag.Vegan))
        advanceTimeBy(301)

        assertThat(repository.refreshed).containsExactly(SearchQuery.Catalog)
        assertThat(filters.value.applied).isEqualTo(SearchQuery.Catalog)
        assertThat(filters.value.draft).isEqualTo(SearchQuery(dietaryTags = setOf(DietaryTag.Vegan)))
    }

    @Test
    fun `show recipes searches the draft and keeps the search text`() = runTest {
        val viewModel = viewModel()
        val filters = filtersOf(viewModel)
        viewModel.onAction(RecipeListAction.ChangeSearchText("curry"))
        advanceTimeBy(301)
        viewModel.onAction(RecipeListAction.OpenFilters)
        viewModel.onAction(RecipeListAction.ToggleDifficulty(Difficulty.Easy))

        viewModel.onAction(RecipeListAction.ShowRecipes)

        val expected = SearchQuery(text = "curry", difficulties = setOf(Difficulty.Easy))
        assertThat(repository.refreshed.last()).isEqualTo(expected)
        assertThat(filters.value.applied).isEqualTo(expected)
        assertThat(filters.value.applied.activeFilterCount).isEqualTo(1)
    }

    @Test
    fun `show recipes with the same filters does not call the server again`() = runTest {
        val viewModel = viewModel()
        viewModel.onAction(RecipeListAction.OpenFilters)

        viewModel.onAction(RecipeListAction.ShowRecipes)

        assertThat(repository.refreshed).containsExactly(SearchQuery.Catalog)
    }

    @Test
    fun `vegan and halal shows no results`() = runTest {
        val veganHalal = SearchQuery(dietaryTags = setOf(DietaryTag.Vegan, DietaryTag.Halal))
        repository.savedFor(veganHalal).value = SearchResult(recipes = emptyList(), hasMore = false)
        val viewModel = viewModel()

        viewModel.uiState.test {
            viewModel.onAction(RecipeListAction.OpenFilters)
            viewModel.onAction(RecipeListAction.ToggleDietaryTag(DietaryTag.Vegan))
            viewModel.onAction(RecipeListAction.ToggleDietaryTag(DietaryTag.Halal))
            viewModel.onAction(RecipeListAction.ShowRecipes)
            repository.endRefresh(CallOutcome.Success)
            repository.endRefresh(CallOutcome.Success)

            assertThat(expectMostRecentItem()).isEqualTo(RecipeListUiState.NoResults)
        }
    }

    @Test
    fun `reopening the sheet drops a draft that was not shown`() = runTest {
        val viewModel = viewModel()
        val filters = filtersOf(viewModel)
        viewModel.onAction(RecipeListAction.OpenFilters)
        viewModel.onAction(RecipeListAction.ToggleDietaryTag(DietaryTag.Vegan))

        viewModel.onAction(RecipeListAction.OpenFilters)

        assertThat(filters.value.draft).isEqualTo(SearchQuery.Catalog)
    }

    @Test
    fun `clear all in the sheet clears the draft only`() = runTest {
        val viewModel = viewModel()
        val filters = filtersOf(viewModel)
        applyFilters(viewModel, DietaryTag.Vegan)
        viewModel.onAction(RecipeListAction.OpenFilters)

        viewModel.onAction(RecipeListAction.ClearDraftFilters)

        assertThat(filters.value.draft).isEqualTo(SearchQuery.Catalog)
        assertThat(filters.value.applied).isEqualTo(SearchQuery(dietaryTags = setOf(DietaryTag.Vegan)))
    }

    @Test
    fun `removing a chip searches without that filter`() = runTest {
        val viewModel = viewModel()
        applyFilters(viewModel, DietaryTag.Vegan, DietaryTag.GlutenFree)

        viewModel.onAction(RecipeListAction.RemoveFilter(ActiveFilter.Diet(DietaryTag.Vegan)))

        assertThat(repository.refreshed.last()).isEqualTo(SearchQuery(dietaryTags = setOf(DietaryTag.GlutenFree)))
    }

    @Test
    fun `clear all filters keeps the search text`() = runTest {
        val viewModel = viewModel()
        viewModel.onAction(RecipeListAction.ChangeSearchText("pao"))
        advanceTimeBy(301)
        applyFilters(viewModel, DietaryTag.Vegan)

        viewModel.onAction(RecipeListAction.ClearAllFilters)

        assertThat(repository.refreshed.last()).isEqualTo(SearchQuery(text = "pao"))
    }

    @Test
    fun `typing keeps the applied filters`() = runTest {
        val viewModel = viewModel()
        applyFilters(viewModel, DietaryTag.Vegan)

        viewModel.onAction(RecipeListAction.ChangeSearchText("curry"))
        advanceTimeBy(301)

        assertThat(repository.refreshed.last())
            .isEqualTo(SearchQuery(text = "curry", dietaryTags = setOf(DietaryTag.Vegan)))
    }

    private fun applyFilters(viewModel: RecipeListViewModel, vararg tags: DietaryTag) {
        viewModel.onAction(RecipeListAction.OpenFilters)
        tags.forEach { viewModel.onAction(RecipeListAction.ToggleDietaryTag(it)) }
        viewModel.onAction(RecipeListAction.ShowRecipes)
    }
}
