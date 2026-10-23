package com.railson.recipes.feature.recipelist.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.railson.recipes.feature.recipelist.filter.RecipeFiltersUiState
import com.railson.recipes.feature.recipelist.filter.without
import com.railson.recipes.feature.recipelist.filter.withFiltersOf
import com.railson.recipes.feature.recipelist.filter.withoutFilters
import com.railson.recipes.recipes.domain.model.CallOutcome
import com.railson.recipes.recipes.domain.model.SearchQuery
import com.railson.recipes.recipes.domain.model.SearchResult
import com.railson.recipes.recipes.domain.usecase.CountRecipesUseCase
import com.railson.recipes.recipes.domain.usecase.ObserveSearchUseCase
import com.railson.recipes.recipes.domain.usecase.RefreshSearchUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

/**
 * Shows what Room has for the current search right away, and refreshes it every time the search opens
 * (docs/specs/data.md, Flow). The list opens on the catalog. The filter sheet edits a draft, counted on
 * the server, that becomes the search on "Show N recipes".
 */
@OptIn(ExperimentalCoroutinesApi::class)
internal class RecipeListViewModel(
    private val observeSearch: ObserveSearchUseCase,
    private val refreshSearch: RefreshSearchUseCase,
    private val countRecipes: CountRecipesUseCase,
) : ViewModel() {

    private val query = MutableStateFlow(SearchQuery.Catalog)
    private val currentCall = MutableStateFlow(SearchCall(SearchQuery.Catalog, LastCall.Running))
    private val effectChannel = Channel<RecipeListEffect>(Channel.BUFFERED)
    val effects: Flow<RecipeListEffect> = effectChannel.receiveAsFlow()

    val uiState: StateFlow<RecipeListUiState> = query
        .flatMapLatest { search ->
            combine(observeSearch(search), currentCall) { saved, call -> screenState(search, saved, call.lastCall) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), RecipeListUiState.Loading)

    private val draft = MutableStateFlow(DraftFilters())

    /** The sheet has its own life next to the list's states, so it has its own state. */
    val filters: StateFlow<RecipeFiltersUiState> = combine(query, draft) { applied, draft ->
        RecipeFiltersUiState(applied = applied, draft = applied.withFiltersOf(draft.query), draftCount = draft.count)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), RecipeFiltersUiState())

    private var searchTextJob: Job? = null
    private var refreshJob: Job? = null
    private var countJob: Job? = null

    init {
        refresh()
    }

    fun onAction(action: RecipeListAction) {
        when (action) {
            RecipeListAction.Refresh -> refresh()
            is RecipeListAction.ChangeSearchText -> changeSearchText(action.text)
            is RecipeListAction.OpenRecipe -> effectChannel.trySend(RecipeListEffect.OpenRecipe(action.id))
            RecipeListAction.OpenFilters -> openFilters()
            is RecipeListAction.ToggleDietaryTag -> changeDraft {
                copy(dietaryTags = dietaryTags.toggle(action.tag))
            }
            is RecipeListAction.ChangeServingsRange -> changeDraft { copy(servingsRange = action.range) }
            is RecipeListAction.ToggleDifficulty -> changeDraft {
                copy(difficulties = difficulties.toggle(action.difficulty))
            }
            RecipeListAction.ClearDraftFilters -> changeDraft { withoutFilters() }
            RecipeListAction.ShowRecipes -> search(query.value.withFiltersOf(draft.value.query))
            is RecipeListAction.RemoveFilter -> search(query.value.without(action.filter))
            RecipeListAction.ClearAllFilters -> search(query.value.withoutFilters())
        }
    }

    private fun changeSearchText(text: String) {
        searchTextJob?.cancel()
        searchTextJob = viewModelScope.launch {
            delay(SEARCH_TEXT_WAIT)
            search(query.value.copy(text = text.trim()))
        }
    }

    private fun search(newQuery: SearchQuery) {
        if (newQuery == query.value) return
        query.value = newQuery
        refresh()
    }

    /** Counts right away: the applied filters have nothing to wait for. The last number stays until then. */
    private fun openFilters() {
        draft.update { DraftFilters(query = query.value, count = it.count) }
        count(wait = false)
    }

    private fun changeDraft(change: SearchQuery.() -> SearchQuery) {
        draft.update { it.copy(query = it.query.change()) }
        count(wait = true)
    }

    /** The count keeps the last number while the next one loads, so the button doesn't flicker. */
    private fun count(wait: Boolean) {
        countJob?.cancel()
        countJob = viewModelScope.launch {
            if (wait) delay(FILTER_COUNT_WAIT)
            // The search text may have changed since the sheet opened. The count is for what "Show" applies.
            val count = countRecipes(query.value.withFiltersOf(draft.value.query))
            draft.update { it.copy(count = count) }
        }
    }

    /** No automatic retry: only the reader calls the server again. A new search cancels the old search's call. */
    private fun refresh() {
        val search = query.value
        if (refreshJob?.isActive == true && currentCall.value.query == search) return
        refreshJob?.cancel()
        currentCall.value = SearchCall(search, LastCall.Running)
        refreshJob = viewModelScope.launch {
            val call = refreshSearch(search).toLastCall()
            currentCall.value = SearchCall(search, call)
            // With saved recipes on screen, a failed refresh is only a snackbar.
            val hasSavedRecipes = observeSearch(search).first()?.recipes.orEmpty().isNotEmpty()
            val snackbar = when (call) {
                LastCall.Offline -> RecipeListEffect.ShowOfflineSnackbar
                LastCall.Failed -> RecipeListEffect.ShowRefreshFailedSnackbar
                LastCall.Running, LastCall.Succeeded -> null
            }
            if (snackbar != null && hasSavedRecipes) effectChannel.send(snackbar)
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
        val SEARCH_TEXT_WAIT = 300.milliseconds
        val FILTER_COUNT_WAIT = 300.milliseconds
    }
}

/** What the sheet edits, and the server's count for it. Its search text is never used. */
private data class DraftFilters(val query: SearchQuery = SearchQuery.Catalog, val count: Int? = null)

private fun <T> Set<T>.toggle(item: T): Set<T> = if (item in this) this - item else this + item

/** How the last call to the server went, as the screen sees it. */
private enum class LastCall { Running, Succeeded, Offline, Failed }

/** The last call and the search it was made for. A new search sets it to running before the screen reads it. */
private data class SearchCall(val query: SearchQuery, val lastCall: LastCall)

private fun CallOutcome.toLastCall(): LastCall = when (this) {
    CallOutcome.Success -> LastCall.Succeeded
    CallOutcome.NoConnection, CallOutcome.Timeout -> LastCall.Offline
    CallOutcome.BadRequest,
    CallOutcome.NotFound,
    CallOutcome.RateLimited,
    CallOutcome.ServerError,
    CallOutcome.Unavailable,
    CallOutcome.BadData,
    -> LastCall.Failed
}

/** The "Screen states" table in docs/specs/data.md. */
private fun screenState(query: SearchQuery, saved: SearchResult?, lastCall: LastCall): RecipeListUiState {
    val recipes = saved?.recipes.orEmpty()
    return when {
        recipes.isNotEmpty() -> RecipeListUiState.Content(recipes, isRefreshing = lastCall == LastCall.Running)
        // A saved result with no recipes shows the failure like an unsaved one (data.md, Screen states).
        lastCall == LastCall.Offline -> RecipeListUiState.Offline
        lastCall == LastCall.Failed -> RecipeListUiState.Error
        // Until Room shows the saved search, the call that saved it still counts as running.
        lastCall == LastCall.Running || saved == null -> RecipeListUiState.Loading
        query == SearchQuery.Catalog -> RecipeListUiState.EmptyCatalogue
        else -> RecipeListUiState.NoResults
    }
}
