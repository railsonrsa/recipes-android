package com.railson.recipes.feature.recipelist.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.railson.recipes.recipes.domain.model.CallOutcome
import com.railson.recipes.recipes.domain.model.SearchQuery
import com.railson.recipes.recipes.domain.model.SearchResult
import com.railson.recipes.recipes.domain.usecase.ObserveSearchUseCase
import com.railson.recipes.recipes.domain.usecase.RefreshSearchUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Shows what Room has for the catalog right away, and refreshes it on open (docs/specs/data.md, Flow). */
internal class RecipeListViewModel(
    private val observeSearch: ObserveSearchUseCase,
    private val refreshSearch: RefreshSearchUseCase,
) : ViewModel() {

    private val lastCall = MutableStateFlow(LastCall.Running)
    private val effectChannel = Channel<RecipeListEffect>(Channel.BUFFERED)
    val effects: Flow<RecipeListEffect> = effectChannel.receiveAsFlow()

    val uiState: StateFlow<RecipeListUiState> = combine(observeSearch(SearchQuery.Catalog), lastCall, ::screenState)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), RecipeListUiState.Loading)

    private var refreshJob: Job? = null

    init {
        refresh()
    }

    fun onAction(action: RecipeListAction) {
        when (action) {
            RecipeListAction.Refresh -> refresh()
            is RecipeListAction.OpenRecipe -> effectChannel.trySend(RecipeListEffect.OpenRecipe(action.id))
        }
    }

    /** No automatic retry: only the reader calls the server again. */
    private fun refresh() {
        if (refreshJob?.isActive == true) return
        lastCall.value = LastCall.Running
        refreshJob = viewModelScope.launch {
            val call = refreshSearch(SearchQuery.Catalog).toLastCall()
            lastCall.value = call
            // With saved recipes on screen, a failed refresh is only a snackbar.
            val hasSavedRecipes = observeSearch(SearchQuery.Catalog).first()?.recipes.orEmpty().isNotEmpty()
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
    }
}

/** How the last call to the server went, as the screen sees it. */
private enum class LastCall { Running, Succeeded, Offline, Failed }

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
private fun screenState(saved: SearchResult?, lastCall: LastCall): RecipeListUiState {
    val recipes = saved?.recipes.orEmpty()
    return when {
        recipes.isNotEmpty() -> RecipeListUiState.Content(recipes, isRefreshing = lastCall == LastCall.Running)
        // A saved result with no recipes shows the failure like an unsaved one (data.md, Screen states).
        lastCall == LastCall.Offline -> RecipeListUiState.Offline
        lastCall == LastCall.Failed -> RecipeListUiState.Error
        // Until Room shows the saved search, the call that saved it still counts as running.
        lastCall == LastCall.Running || saved == null -> RecipeListUiState.Loading
        // Only the catalog for now. A search with text or filters shows "No results" (#23).
        else -> RecipeListUiState.EmptyCatalogue
    }
}
