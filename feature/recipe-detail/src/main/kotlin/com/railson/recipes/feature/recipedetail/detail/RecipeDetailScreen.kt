package com.railson.recipes.feature.recipedetail.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.railson.recipes.core.ui.components.RecipeImage
import com.railson.recipes.core.ui.preview.ThemePreviews
import com.railson.recipes.core.ui.theme.RecipesTheme
import com.railson.recipes.core.ui.theme.Spacing
import com.railson.recipes.feature.recipedetail.R
import com.railson.recipes.feature.recipedetail.components.Description
import com.railson.recipes.feature.recipedetail.components.DetailDietTags
import com.railson.recipes.feature.recipedetail.components.PreviewRecipes
import com.railson.recipes.feature.recipedetail.components.StatBoxes
import com.railson.recipes.recipes.domain.model.scaledIngredients
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
internal fun RecipeDetailScreen(
    id: String,
    onBack: () -> Unit,
    viewModel: RecipeDetailViewModel = koinViewModel { parametersOf(id) },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    RecipeDetailLayout(uiState = uiState, onBack = onBack)
}

@Composable
internal fun RecipeDetailLayout(
    uiState: RecipeDetailUiState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        // The hero image goes under the status bar. Each state handles the insets.
        contentWindowInsets = WindowInsets(0),
    ) { padding ->
        val stateModifier = Modifier.padding(padding)
        when (uiState) {
            // Room answers in a moment. An empty panel avoids a spinner flash.
            RecipeDetailUiState.Loading -> Box(stateModifier.fillMaxSize())
            RecipeDetailUiState.NotAvailable -> RecipeDetailNotAvailable(onBack = onBack, modifier = stateModifier)
            is RecipeDetailUiState.Content -> RecipeDetailContent(
                content = uiState,
                onBack = onBack,
                modifier = stateModifier,
            )
        }
    }
}

/** The hero image, then the content panel (DESIGN.md 3.9). */
@Composable
internal fun RecipeDetailContent(
    content: RecipeDetailUiState.Content,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val recipe = content.recipe
    val panelPadding = Modifier.padding(horizontal = Spacing.lg)
    val navigationBar = WindowInsets.navigationBars.asPaddingValues()
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = navigationBar.calculateBottomPadding() + Spacing.xl),
    ) {
        item(key = "hero") { Hero(imageUrl = recipe.imageUrl, onBack = onBack) }
        item(key = "title") {
            Column(modifier = panelPadding) {
                if (recipe.cuisine.isNotBlank()) {
                    Text(
                        text = recipe.cuisine,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                }
                Text(text = recipe.title, style = MaterialTheme.typography.headlineSmall)
            }
        }
        item(key = "stats") { StatBoxes(recipe = recipe, modifier = panelPadding.padding(top = Spacing.lg)) }
        if (recipe.dietaryTags.isNotEmpty()) {
            item(key = "tags") {
                DetailDietTags(tags = recipe.dietaryTags, modifier = panelPadding.padding(top = Spacing.lg))
            }
        }
        if (recipe.description.isNotBlank()) {
            item(key = "description") {
                Description(text = recipe.description, modifier = panelPadding.padding(top = Spacing.lg))
            }
        }
    }
}

/** 232 dp, under the status bar, with the panel's rounded top over its bottom 28 dp. */
@Composable
private fun Hero(imageUrl: String?, onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(232.dp),
    ) {
        RecipeImage(url = imageUrl, modifier = Modifier.fillMaxSize())
        BackButton(
            onClick = onBack,
            modifier = Modifier
                .statusBarsPadding()
                .padding(Spacing.sm),
        )
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerLowest,
            shape = RoundedCornerShape(topStart = PanelRadius, topEnd = PanelRadius),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(PanelRadius),
        ) {}
    }
}

@Composable
internal fun BackButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(
        onClick = onClick,
        modifier = modifier,
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
        shape = CircleShape,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_arrow_back),
            contentDescription = stringResource(R.string.recipe_detail_back),
            modifier = Modifier.size(24.dp),
        )
    }
}

private val PanelRadius = 28.dp

@ThemePreviews
@Composable
private fun RecipeDetailContentPreview() {
    RecipesTheme {
        RecipeDetailLayout(
            uiState = PreviewRecipes.margherita.let {
                val scaled = it.servings + 2
                RecipeDetailUiState.Content(
                    recipe = it,
                    scaledServings = scaled,
                    ingredients = it.scaledIngredients(scaled),
                )
            },
            onBack = {},
        )
    }
}

@ThemePreviews
@Composable
private fun RecipeDetailEdgeCasesPreview() {
    RecipesTheme {
        RecipeDetailLayout(
            uiState = PreviewRecipes.edgeCases.let {
                RecipeDetailUiState.Content(recipe = it, scaledServings = it.servings, ingredients = it.ingredients)
            },
            onBack = {},
        )
    }
}
