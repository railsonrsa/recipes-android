package com.railson.recipes.feature.recipelist.list

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.railson.recipes.core.ui.preview.ThemePreviews
import com.railson.recipes.core.ui.theme.RecipesTheme
import com.railson.recipes.core.ui.theme.Spacing
import com.railson.recipes.feature.recipelist.R
import com.railson.recipes.feature.recipelist.components.PreviewRecipes
import com.railson.recipes.feature.recipelist.components.RecipeCard
import com.railson.recipes.feature.recipelist.components.RecipeSearchBar
import com.railson.recipes.feature.recipelist.filter.ActiveFilterChips
import com.railson.recipes.feature.recipelist.filter.RecipeFilterSheet
import com.railson.recipes.feature.recipelist.filter.RecipeFiltersUiState
import com.railson.recipes.feature.recipelist.filter.activeFilters
import com.railson.recipes.recipes.domain.model.DietaryTag
import com.railson.recipes.recipes.domain.model.IngredientSide
import com.railson.recipes.recipes.domain.model.SearchQuery
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun RecipeListScreen(
    onRecipeClick: (String) -> Unit,
    topBarActions: @Composable RowScope.() -> Unit,
    viewModel: RecipeListViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val filters by viewModel.filters.collectAsStateWithLifecycle()
    var showFilters by rememberSaveable { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    // The sheet is its own window over the screen, so its snackbars need their own host.
    val sheetSnackbarHostState = remember { SnackbarHostState() }
    // The texts live on screen, so typing never waits for the ViewModel. The ViewModel gets each change.
    val searchText = rememberTextSentTo(viewModel) { RecipeListAction.ChangeSearchText(it) }
    val instructionText = rememberTextSentTo(viewModel) { RecipeListAction.ChangeInstructionSearch(it) }
    RecipeListEffects(viewModel, snackbarHostState, sheetSnackbarHostState, onRecipeClick)
    val showsList = uiState is RecipeListUiState.Content
    LaunchedEffect(showsList) {
        // The snackbars are about the saved list. Another state says it all on its own.
        if (!showsList) snackbarHostState.currentSnackbarData?.dismiss()
    }
    RecipeListLayout(
        uiState = uiState,
        filters = filters,
        searchText = searchText,
        snackbarHostState = snackbarHostState,
        onAction = { action ->
            if (action == RecipeListAction.OpenFilters) {
                // The sheet starts from the applied filters, the instruction field too.
                instructionText.setTextAndPlaceCursorAtEnd(filters.applied.instructionSearch)
                showFilters = true
            }
            viewModel.onAction(action)
        },
        topBarActions = topBarActions,
    )
    if (showFilters) {
        RecipeFilterSheet(
            filters = filters,
            instructionText = instructionText,
            snackbarHostState = sheetSnackbarHostState,
            onAction = { action ->
                if (action == RecipeListAction.ClearDraftFilters) instructionText.clearText()
                viewModel.onAction(action)
            },
            onDismiss = { showFilters = false },
        )
    }
}

@Composable
private fun rememberTextSentTo(viewModel: RecipeListViewModel, action: (String) -> RecipeListAction): TextFieldState {
    val text = rememberTextFieldState()
    val currentAction by rememberUpdatedState(action)
    LaunchedEffect(text, viewModel) {
        snapshotFlow { text.text.toString() }.collect { viewModel.onAction(currentAction(it)) }
    }
    return text
}

/** Shows the snackbars, on the screen or in the sheet, and leaves for a recipe. */
@Composable
private fun RecipeListEffects(
    viewModel: RecipeListViewModel,
    snackbarHostState: SnackbarHostState,
    sheetSnackbarHostState: SnackbarHostState,
    onRecipeClick: (String) -> Unit,
) {
    val resources = LocalResources.current
    val retry = stringResource(R.string.recipe_list_retry)
    val currentOnRecipeClick by rememberUpdatedState(onRecipeClick)
    LaunchedEffect(viewModel) {
        // A new snackbar replaces the one on its host: cancelling showSnackbar dismisses it.
        // Each host has its own job, so the sheet's snackbar never dismisses the list's.
        var listSnackbar: Job? = null
        var sheetSnackbar: Job? = null
        viewModel.effects.collect { effect ->
            when (effect) {
                RecipeListEffect.ShowOfflineSnackbar, RecipeListEffect.ShowRefreshFailedSnackbar -> {
                    val message = if (effect == RecipeListEffect.ShowOfflineSnackbar) {
                        resources.getString(R.string.recipe_list_snackbar_offline)
                    } else {
                        resources.getString(R.string.recipe_list_snackbar_refresh_failed)
                    }
                    listSnackbar?.cancel()
                    listSnackbar = launch {
                        // With an action, the default duration is Indefinite.
                        val result = snackbarHostState.showSnackbar(
                            message,
                            actionLabel = retry,
                            duration = SnackbarDuration.Long,
                        )
                        if (result == SnackbarResult.ActionPerformed) viewModel.onAction(RecipeListAction.Refresh)
                    }
                }
                is RecipeListEffect.ShowIngredientMoved -> {
                    sheetSnackbar?.cancel()
                    sheetSnackbar = launch {
                        sheetSnackbarHostState.showSnackbar(resources.getString(effect.side.movedMessage, effect.word))
                    }
                }
                is RecipeListEffect.OpenRecipe -> currentOnRecipeClick(effect.id)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
// The stateless screen: its states, the hoisted search text, the snackbars and callbacks.
@Suppress("LongParameterList")
@Composable
internal fun RecipeListLayout(
    uiState: RecipeListUiState,
    filters: RecipeFiltersUiState,
    searchText: TextFieldState,
    snackbarHostState: SnackbarHostState,
    onAction: (RecipeListAction) -> Unit,
    topBarActions: @Composable RowScope.() -> Unit,
    modifier: Modifier = Modifier,
) {
    val refresh = { onAction(RecipeListAction.Refresh) }
    val layoutDirection = LocalLayoutDirection.current
    Scaffold(
        modifier = modifier,
        topBar = {
            RecipeListTopBar(
                uiState = uiState,
                filters = filters,
                searchText = searchText,
                onAction = onAction,
                topBarActions = topBarActions,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        val statePadding = Modifier.padding(padding)
        when (uiState) {
            RecipeListUiState.Loading -> RecipeListLoading(statePadding)
            RecipeListUiState.Offline -> RecipeListOffline(onRetry = refresh, modifier = statePadding)
            RecipeListUiState.Error -> RecipeListError(onTryAgain = refresh, modifier = statePadding)
            RecipeListUiState.EmptyCatalogue -> RecipeListEmptyCatalogue(onRefresh = refresh, modifier = statePadding)
            RecipeListUiState.NoResults -> RecipeListNoResults(
                hasFilters = filters.applied.activeFilterCount > 0,
                onClearAllFilters = { onAction(RecipeListAction.ClearAllFilters) },
                modifier = statePadding,
            )
            is RecipeListUiState.Content -> PullToRefreshBox(
                isRefreshing = uiState.isRefreshing,
                onRefresh = refresh,
                // The indicator comes down from under the top app bar. The list draws behind the navigation bar.
                modifier = Modifier.padding(top = padding.calculateTopPadding()),
            ) {
                RecipeListList(
                    content = uiState,
                    onAction = onAction,
                    contentPadding = PaddingValues(
                        start = padding.calculateStartPadding(layoutDirection),
                        end = padding.calculateEndPadding(layoutDirection),
                        bottom = padding.calculateBottomPadding(),
                    ),
                )
            }
        }
    }
}

/** The top app bar, the search bar and the active filter chips. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecipeListTopBar(
    uiState: RecipeListUiState,
    filters: RecipeFiltersUiState,
    searchText: TextFieldState,
    onAction: (RecipeListAction) -> Unit,
    topBarActions: @Composable RowScope.() -> Unit,
) {
    Column {
        TopAppBar(
            title = { Text(stringResource(R.string.recipe_list_title)) },
            actions = topBarActions,
        )
        // An empty catalogue has nothing to search (DESIGN.md 3.7).
        if (uiState == RecipeListUiState.EmptyCatalogue) return@Column
        RecipeSearchBar(
            state = searchText,
            enabled = searchBarEnabled(uiState, searchText, filters.applied),
            activeFilterCount = filters.applied.activeFilterCount,
            onFiltersClick = { onAction(RecipeListAction.OpenFilters) },
            modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.sm),
        )
        val activeFilters = filters.applied.activeFilters
        if (activeFilters.isNotEmpty()) {
            ActiveFilterChips(
                filters = activeFilters,
                onRemove = { onAction(RecipeListAction.RemoveFilter(it)) },
                modifier = Modifier.padding(bottom = Spacing.sm),
            )
        }
        // The cards scroll under the pinned bar (DESIGN.md 3.3).
        if (uiState is RecipeListUiState.Content) HorizontalDivider(color = RecipesTheme.colors.hairline)
    }
}

/**
 * Disabled while the catalog loads for the first time or fails (DESIGN.md 3.1 and 3.6).
 * With text or filters in it, the reader can always change the search.
 */
private fun searchBarEnabled(uiState: RecipeListUiState, searchText: TextFieldState, applied: SearchQuery): Boolean =
    searchText.text.isNotBlank() ||
        applied.activeFilterCount > 0 ||
        (uiState != RecipeListUiState.Loading && uiState != RecipeListUiState.Error)

/** "15 recipes", the recipe cards, then the next page or the end (DESIGN.md 3.2 to 3.4). */
@Composable
internal fun RecipeListList(
    content: RecipeListUiState.Content,
    onAction: (RecipeListAction) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val recipes = content.recipes
    val layoutDirection = LocalLayoutDirection.current
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = contentPadding.calculateStartPadding(layoutDirection) + Spacing.lg,
            top = contentPadding.calculateTopPadding(),
            end = contentPadding.calculateEndPadding(layoutDirection) + Spacing.lg,
            bottom = contentPadding.calculateBottomPadding() + Spacing.lg,
        ),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        if (recipes.isNotEmpty()) {
            item(key = "count") {
                Text(
                    text = pluralStringResource(R.plurals.recipe_list_count, content.totalItems, content.totalItems),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = Spacing.lg),
                )
            }
        }
        items(recipes, key = { it.id }) { recipe ->
            RecipeCard(
                recipe = recipe,
                onClick = { onAction(RecipeListAction.OpenRecipe(recipe.id)) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
        item(key = "end", contentType = content.listEnd) {
            when (content.listEnd) {
                ListEnd.LoadingMore -> {
                    // The row coming into view loads the next page. A new page or the end of a refresh asks
                    // again while it stays in view. The ViewModel ignores the extra asks.
                    LaunchedEffect(recipes.size, content.isRefreshing) { onAction(RecipeListAction.LoadNextPage) }
                    RecipeListLoadingMore()
                }
                ListEnd.LoadMoreFailed ->
                    RecipeListLoadMoreFailed(onRetry = { onAction(RecipeListAction.LoadNextPage) })
                ListEnd.AllLoaded -> RecipeListAllLoaded(totalItems = content.totalItems)
            }
        }
    }
}

@ThemePreviews
@Composable
private fun RecipeListLayoutPreview() {
    RecipesTheme {
        RecipeListLayout(
            uiState = RecipeListUiState.Content(
                recipes = PreviewRecipes.all,
                isRefreshing = false,
                totalItems = PreviewRecipes.all.size,
                listEnd = ListEnd.AllLoaded,
            ),
            filters = RecipeFiltersUiState(),
            searchText = rememberTextFieldState(),
            snackbarHostState = remember { SnackbarHostState() },
            onAction = {},
            topBarActions = {},
        )
    }
}

@ThemePreviews
@Composable
private fun RecipeListLayoutNoResultsPreview() {
    RecipesTheme {
        RecipeListLayout(
            uiState = RecipeListUiState.NoResults,
            filters = RecipeFiltersUiState(
                applied = SearchQuery(dietaryTags = setOf(DietaryTag.Vegan, DietaryTag.Halal)),
            ),
            searchText = rememberTextFieldState(),
            snackbarHostState = remember { SnackbarHostState() },
            onAction = {},
            topBarActions = {},
        )
    }
}

@ThemePreviews
@Composable
private fun RecipeListListPreview() {
    RecipesTheme {
        RecipeListList(
            content = RecipeListUiState.Content(
                recipes = PreviewRecipes.edgeCases,
                isRefreshing = false,
                totalItems = PreviewRecipes.edgeCases.size + 10,
                listEnd = ListEnd.LoadingMore,
            ),
            onAction = {},
            contentPadding = PaddingValues(),
        )
    }
}

@get:StringRes
private val IngredientSide.movedMessage: Int
    get() = when (this) {
        IngredientSide.Included -> R.string.recipe_filters_ingredient_moved_to_included
        IngredientSide.Excluded -> R.string.recipe_filters_ingredient_moved_to_excluded
    }
