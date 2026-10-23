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
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.collectLatest
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
import com.railson.recipes.recipes.domain.model.Recipe
import com.railson.recipes.recipes.domain.model.SearchQuery
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
    val resources = LocalResources.current
    val offlineMessage = stringResource(R.string.recipe_list_snackbar_offline)
    val refreshFailedMessage = stringResource(R.string.recipe_list_snackbar_refresh_failed)
    val retry = stringResource(R.string.recipe_list_retry)
    val currentOnRecipeClick by rememberUpdatedState(onRecipeClick)
    // The text lives on screen, so typing never waits for the ViewModel. The ViewModel gets each change.
    val searchText = rememberTextFieldState()
    LaunchedEffect(searchText, viewModel) {
        snapshotFlow { searchText.text.toString() }
            .collect { viewModel.onAction(RecipeListAction.ChangeSearchText(it)) }
    }
    LaunchedEffect(viewModel) {
        // A new snackbar replaces the one on screen: cancelling showSnackbar dismisses it.
        viewModel.effects.collectLatest { effect ->
            val message = when (effect) {
                RecipeListEffect.ShowOfflineSnackbar -> offlineMessage
                RecipeListEffect.ShowRefreshFailedSnackbar -> refreshFailedMessage
                is RecipeListEffect.OpenRecipe -> {
                    currentOnRecipeClick(effect.id)
                    return@collectLatest
                }
                is RecipeListEffect.ShowIngredientMoved -> {
                    sheetSnackbarHostState.showSnackbar(resources.getString(effect.side.movedMessage, effect.word))
                    return@collectLatest
                }
            }
            // With an action, the default duration is Indefinite.
            val result = snackbarHostState.showSnackbar(message, actionLabel = retry, duration = SnackbarDuration.Long)
            if (result == SnackbarResult.ActionPerformed) viewModel.onAction(RecipeListAction.Refresh)
        }
    }
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
            if (action == RecipeListAction.OpenFilters) showFilters = true
            viewModel.onAction(action)
        },
        topBarActions = topBarActions,
    )
    if (showFilters) {
        RecipeFilterSheet(
            filters = filters,
            snackbarHostState = sheetSnackbarHostState,
            onAction = viewModel::onAction,
            onDismiss = { showFilters = false },
        )
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
                    recipes = uiState.recipes,
                    onRecipeClick = { id -> onAction(RecipeListAction.OpenRecipe(id)) },
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

/** "10 recipes", then the recipe cards (DESIGN.md 3.2). */
@Composable
internal fun RecipeListList(
    recipes: List<Recipe>,
    onRecipeClick: (String) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
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
                // Only the loaded recipes until paging (#26) brings the search total.
                Text(
                    text = pluralStringResource(R.plurals.recipe_list_count, recipes.size, recipes.size),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = Spacing.lg),
                )
            }
        }
        items(recipes, key = { it.id }) { recipe ->
            RecipeCard(recipe = recipe, onClick = { onRecipeClick(recipe.id) }, modifier = Modifier.fillMaxWidth())
        }
    }
}

@ThemePreviews
@Composable
private fun RecipeListLayoutPreview() {
    RecipesTheme {
        RecipeListLayout(
            uiState = RecipeListUiState.Content(recipes = PreviewRecipes.all, isRefreshing = false),
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
        RecipeListList(recipes = PreviewRecipes.edgeCases, onRecipeClick = {}, contentPadding = PaddingValues())
    }
}

@get:StringRes
private val IngredientSide.movedMessage: Int
    get() = when (this) {
        IngredientSide.Included -> R.string.recipe_filters_ingredient_moved_to_included
        IngredientSide.Excluded -> R.string.recipe_filters_ingredient_moved_to_excluded
    }
