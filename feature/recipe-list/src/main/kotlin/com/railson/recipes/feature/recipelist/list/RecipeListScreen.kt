package com.railson.recipes.feature.recipelist.list

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
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
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.railson.recipes.core.ui.preview.ThemePreviews
import com.railson.recipes.core.ui.theme.RecipesTheme
import com.railson.recipes.core.ui.theme.Spacing
import com.railson.recipes.feature.recipelist.R
import com.railson.recipes.recipes.domain.model.Recipe
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Duration

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
        RecipeListTitles(recipes = uiState.recipes, contentPadding = padding)
    }
}

/** A plain list of titles. The cards come with issue #20. */
@Composable
internal fun RecipeListTitles(
    recipes: List<Recipe>,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier.fillMaxSize(), contentPadding = contentPadding) {
        items(recipes, key = { it.id }) { recipe ->
            Text(
                text = recipe.title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.lg, vertical = Spacing.md),
            )
        }
    }
}

@ThemePreviews
@Composable
private fun RecipeListLayoutPreview() {
    RecipesTheme {
        RecipeListLayout(
            uiState = RecipeListUiState(
                recipes = listOf(previewRecipe("1", "Greek Salad"), previewRecipe("2", "Pão de Queijo")),
            ),
            topBarActions = {},
        )
    }
}

@ThemePreviews
@Composable
private fun RecipeListTitlesPreview() {
    RecipesTheme {
        RecipeListTitles(recipes = listOf(previewRecipe("1", "Greek Salad")), contentPadding = PaddingValues())
    }
}

private fun previewRecipe(id: String, title: String) = Recipe(
    id = id,
    title = title,
    description = "",
    servings = 2,
    prepTime = Duration.ZERO,
    cookTime = Duration.ZERO,
    difficulty = null,
    cuisine = "",
    rating = null,
    imageUrl = null,
    dietaryTags = emptySet(),
    ingredients = emptyList(),
    instructions = emptyList(),
)
