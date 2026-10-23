package com.railson.recipes.feature.recipelist.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.railson.recipes.feature.recipelist.filter.RecipeFiltersUiState
import com.railson.recipes.feature.recipelist.filter.without
import com.railson.recipes.feature.recipelist.filter.withoutFilters
import com.railson.recipes.recipes.domain.model.CallOutcome
import com.railson.recipes.recipes.domain.model.IngredientSide
import com.railson.recipes.recipes.domain.model.SearchQuery
import com.railson.recipes.recipes.domain.model.SearchResult
import com.railson.recipes.recipes.domain.usecase.CountRecipesUseCase
import com.railson.recipes.recipes.domain.usecase.LoadNextPageUseCase
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
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * Shows what Room has for the current search right away, and refreshes it every time the search opens
 * (docs/specs/data.md, Flow). The list opens on the catalog. The filter sheet edits a draft, counted on
 * the server, that becomes the search on "Show N recipes". The next page loads when the end of the list
 * comes into view, never while the search refreshes.
 */
@OptIn(ExperimentalCoroutinesApi::class)
internal class RecipeListViewModel(
    private val observeSearch: ObserveSearchUseCase,
    private val refreshSearch: RefreshSearchUseCase,
    private val loadNextPage: LoadNextPageUseCase,
    private val countRecipes: CountRecipesUseCase,
) : ViewModel() {

    private val query = MutableStateFlow(SearchQuery.Catalog)
    private val currentCall = MutableStateFlow(SearchCall(SearchQuery.Catalog, LastCall.Running))
    private val nextPageStatus = MutableStateFlow(NextPageStatus.Idle)
    private val effectChannel = Channel<RecipeListEffect>(Channel.BUFFERED)
    val effects: Flow<RecipeListEffect> = effectChannel.receiveAsFlow()

    val uiState: StateFlow<RecipeListUiState> = query
        .flatMapLatest { search ->
            combine(observeSearch(search), currentCall, nextPageStatus) { saved, call, nextPage ->
                screenState(search, saved, call.lastCall, nextPage)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), RecipeListUiState.Loading)

    /** What the sheet edits, with the search text "Show N recipes" keeps, and the server's count for it. */
    private val draft = MutableStateFlow(SearchQuery.Catalog)
    private val draftCount = MutableStateFlow<Int?>(null)

    /** The sheet has its own life next to the list's states, so it has its own state. */
    val filters: StateFlow<RecipeFiltersUiState> = combine(query, draft, draftCount, ::RecipeFiltersUiState)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), RecipeFiltersUiState())

    private var searchTextJob: Job? = null
    private var refreshJob: Job? = null
    private var nextPageJob: Job? = null
    private var countJob: Job? = null

    init {
        refresh()
    }

    fun onAction(action: RecipeListAction) {
        when (action) {
            RecipeListAction.Refresh -> refresh()
            RecipeListAction.LoadNextPage -> startNextPage()
            is RecipeListAction.ChangeSearchText -> changeSearchText(action.text)
            is RecipeListAction.OpenRecipe -> effectChannel.trySend(RecipeListEffect.OpenRecipe(action.id))
            RecipeListAction.OpenFilters -> openFilters()
            is RecipeListAction.DraftChange -> changeDraft(action)
            RecipeListAction.ShowRecipes -> showRecipes()
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

    /** The instruction field keeps the reader's spaces while typing. The search doesn't. */
    private fun showRecipes() {
        search(draft.value.copy(instructionSearch = draft.value.instructionSearch.trim()))
    }

    private fun search(newQuery: SearchQuery) {
        if (newQuery == query.value) return
        query.value = newQuery
        draft.update { it.copy(text = newQuery.text) }
        refresh()
    }

    /** Counts right away: the applied filters have nothing to wait for. The last number stays until then. */
    private fun openFilters() {
        draft.value = query.value
        countDraft(after = Duration.ZERO)
    }

    /** An edit that changes nothing doesn't count again, so the on-screen fields can send every change. */
    private fun changeDraft(change: RecipeListAction.DraftChange) {
        val before = draft.value
        val after = when (change) {
            is RecipeListAction.ToggleDietaryTag -> before.copy(dietaryTags = before.dietaryTags.toggle(change.tag))
            is RecipeListAction.ChangeServingsRange -> before.copy(servingsRange = change.range)
            is RecipeListAction.ToggleDifficulty ->
                before.copy(difficulties = before.difficulties.toggle(change.difficulty))
            is RecipeListAction.AddIngredient -> before.withIngredient(change.word, change.side)
            is RecipeListAction.RemoveIngredient -> before.withoutIngredient(change.word)
            is RecipeListAction.ChangeInstructionSearch -> before.copy(instructionSearch = change.text)
            RecipeListAction.ClearDraftFilters -> before.withoutFilters()
        }
        if (after == before) return
        draft.value = after
        if (change is RecipeListAction.AddIngredient) showMoved(before, change.word, change.side)
        countDraft(after = FILTER_COUNT_WAIT)
    }

    private fun showMoved(before: SearchQuery, word: String, side: IngredientSide) {
        val sideBefore = before.sideOf(word)
        if (sideBefore != null && sideBefore != side) {
            effectChannel.trySend(RecipeListEffect.ShowIngredientMoved(word.trim(), side))
        }
    }

    /** The count keeps the last number while the next one loads, so the button doesn't flicker. */
    private fun countDraft(after: Duration) {
        countJob?.cancel()
        countJob = viewModelScope.launch {
            delay(after)
            draftCount.value = countRecipes(draft.value)
        }
    }

    /** No automatic retry: only the reader calls the server again. A new search cancels the old search's call. */
    private fun refresh() {
        val search = query.value
        if (refreshJob?.isActive == true && currentCall.value.query == search) return
        refreshJob?.cancel()
        // Page 1 replaces the saved result, so a next page loading for the old one would land after it.
        // Every new search refreshes, so this also clears the old search's failed next page.
        nextPageJob?.cancel()
        nextPageStatus.value = NextPageStatus.Idle
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

    /** One page at a time, only once the search's refresh ended and only while the server has more. */
    private fun startNextPage() {
        val search = query.value
        if (refreshJob?.isActive == true || nextPageJob?.isActive == true) return
        nextPageJob = viewModelScope.launch {
            // The repository makes no call either, but this keeps "That's all" from flashing "Running".
            if (observeSearch(search).first()?.hasMore != true) return@launch
            nextPageStatus.value = NextPageStatus.Running
            val outcome = loadNextPage(search)
            nextPageStatus.value = if (outcome == CallOutcome.Success) NextPageStatus.Idle else NextPageStatus.Failed
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
        val SEARCH_TEXT_WAIT = 300.milliseconds
        val FILTER_COUNT_WAIT = 300.milliseconds
    }
}

private fun <T> Set<T>.toggle(item: T): Set<T> = if (item in this) this - item else this + item

/** How the last call to the server went, as the screen sees it. */
private enum class LastCall { Running, Succeeded, Offline, Failed }

/** The last call and the search it was made for. A new search sets it to running before the screen reads it. */
private data class SearchCall(val query: SearchQuery, val lastCall: LastCall)

/** How the current search's next page call went. */
private enum class NextPageStatus { Idle, Running, Failed }

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
private fun screenState(
    query: SearchQuery,
    saved: SearchResult?,
    lastCall: LastCall,
    nextPage: NextPageStatus,
): RecipeListUiState {
    val recipes = saved?.recipes.orEmpty()
    return when {
        recipes.isNotEmpty() -> RecipeListUiState.Content(
            recipes = recipes,
            isRefreshing = lastCall == LastCall.Running,
            totalItems = saved?.totalItems ?: recipes.size,
            listEnd = when {
                saved?.hasMore != true -> ListEnd.AllLoaded
                nextPage == NextPageStatus.Failed -> ListEnd.LoadMoreFailed
                else -> ListEnd.LoadingMore
            },
        )
        // A saved result with no recipes shows the failure like an unsaved one (data.md, Screen states).
        lastCall == LastCall.Offline -> RecipeListUiState.Offline
        lastCall == LastCall.Failed -> RecipeListUiState.Error
        // Until Room shows the saved search, the call that saved it still counts as running.
        lastCall == LastCall.Running || saved == null -> RecipeListUiState.Loading
        query == SearchQuery.Catalog -> RecipeListUiState.EmptyCatalogue
        else -> RecipeListUiState.NoResults
    }
}
