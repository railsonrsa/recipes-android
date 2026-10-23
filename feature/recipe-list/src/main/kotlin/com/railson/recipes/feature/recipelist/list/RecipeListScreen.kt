package com.railson.recipes.feature.recipelist.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.collectLatest
import com.railson.recipes.core.ui.preview.ThemePreviews
import com.railson.recipes.core.ui.theme.RecipesTheme
import com.railson.recipes.core.ui.theme.Spacing
import com.railson.recipes.feature.recipelist.R
import com.railson.recipes.feature.recipelist.components.PreviewRecipes
import com.railson.recipes.feature.recipelist.components.RecipeCard
import com.railson.recipes.recipes.domain.model.Recipe
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun RecipeListScreen(
    onRecipeClick: (String) -> Unit,
    topBarActions: @Composable RowScope.() -> Unit,
    viewModel: RecipeListViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val offlineMessage = stringResource(R.string.recipe_list_snackbar_offline)
    val refreshFailedMessage = stringResource(R.string.recipe_list_snackbar_refresh_failed)
    val retry = stringResource(R.string.recipe_list_retry)
    val currentOnRecipeClick by rememberUpdatedState(onRecipeClick)
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
        snackbarHostState = snackbarHostState,
        onAction = viewModel::onAction,
        topBarActions = topBarActions,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RecipeListLayout(
    uiState: RecipeListUiState,
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
            TopAppBar(
                title = { Text(stringResource(R.string.recipe_list_title)) },
                actions = topBarActions,
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
