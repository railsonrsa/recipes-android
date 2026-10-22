package com.railson.recipes.feature.recipelist.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.railson.recipes.recipes.domain.model.SearchQuery
import com.railson.recipes.recipes.domain.usecase.ObserveSearchUseCase
import com.railson.recipes.recipes.domain.usecase.RefreshSearchUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Shows what Room has for the catalog right away, and refreshes it once on open (docs/specs/data.md, Flow). */
internal class RecipeListViewModel(
    observeSearch: ObserveSearchUseCase,
    private val refreshSearch: RefreshSearchUseCase,
) : ViewModel() {

    val uiState: StateFlow<RecipeListUiState> = observeSearch(SearchQuery)
        .map { RecipeListUiState(recipes = it.recipes) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), RecipeListUiState())

    init {
        viewModelScope.launch { refreshSearch(SearchQuery) }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
