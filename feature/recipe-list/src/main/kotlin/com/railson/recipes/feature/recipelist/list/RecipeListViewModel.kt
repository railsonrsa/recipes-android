package com.railson.recipes.feature.recipelist.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.railson.recipes.recipes.domain.model.CallOutcome
import com.railson.recipes.recipes.domain.model.SearchQuery
import com.railson.recipes.recipes.domain.model.SearchResult
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
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

/**
 * Shows what Room has for the current search right away, and refreshes it every time the search opens
 * (docs/specs/data.md, Flow). The list opens on the catalog.
 */
@OptIn(ExperimentalCoroutinesApi::class)
internal class RecipeListViewModel(
    private val observeSearch: ObserveSearchUseCase,
    private val refreshSearch: RefreshSearchUseCase,
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

    private var searchTextJob: Job? = null
    private var refreshJob: Job? = null

    init {
        refresh()
    }

    fun onAction(action: RecipeListAction) {
        when (action) {
            RecipeListAction.Refresh -> refresh()
            is RecipeListAction.ChangeSearchText -> changeSearchText(action.text)
            is RecipeListAction.OpenRecipe -> effectChannel.trySend(RecipeListEffect.OpenRecipe(action.id))
        }
    }

    private fun changeSearchText(text: String) {
        searchTextJob?.cancel()
        searchTextJob = viewModelScope.launch {
            delay(SEARCH_TEXT_WAIT)
            val newQuery = SearchQuery(text = text.trim())
            if (newQuery == query.value) return@launch
            query.value = newQuery
            refresh()
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
    }
}

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
