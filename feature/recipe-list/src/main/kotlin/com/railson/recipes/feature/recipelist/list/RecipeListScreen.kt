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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
    topBarActions: @Composable RowScope.() -> Unit,
    viewModel: RecipeListViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    RecipeListLayout(uiState = uiState, topBarActions = topBarActions)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RecipeListLayout(
    uiState: RecipeListUiState,
    topBarActions: @Composable RowScope.() -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.recipe_list_title)) },
                actions = topBarActions,
            )
        },
    ) { padding ->
        RecipeListList(recipes = uiState.recipes, contentPadding = padding)
    }
}

/** "10 recipes", then the recipe cards (DESIGN.md 3.2). */
@Composable
internal fun RecipeListList(
    recipes: List<Recipe>,
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
            RecipeCard(recipe = recipe, modifier = Modifier.fillMaxWidth())
        }
    }
}

@ThemePreviews
@Composable
private fun RecipeListLayoutPreview() {
    RecipesTheme {
        RecipeListLayout(uiState = RecipeListUiState(recipes = PreviewRecipes.all), topBarActions = {})
    }
}

@ThemePreviews
@Composable
private fun RecipeListListPreview() {
    RecipesTheme {
        RecipeListList(recipes = PreviewRecipes.edgeCases, contentPadding = PaddingValues())
    }
}
