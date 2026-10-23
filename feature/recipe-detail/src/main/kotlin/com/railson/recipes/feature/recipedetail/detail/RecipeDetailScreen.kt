package com.railson.recipes.feature.recipedetail.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.railson.recipes.core.ui.preview.ThemePreviews
import com.railson.recipes.core.ui.theme.RecipesTheme
import com.railson.recipes.core.ui.theme.Spacing
import com.railson.recipes.feature.recipedetail.components.CollapsedTopBar
import com.railson.recipes.feature.recipedetail.components.Description
import com.railson.recipes.feature.recipedetail.components.DetailDietTags
import com.railson.recipes.feature.recipedetail.components.Hero
import com.railson.recipes.feature.recipedetail.components.IngredientRow
import com.railson.recipes.feature.recipedetail.components.IngredientsHeader
import com.railson.recipes.feature.recipedetail.components.InstructionsHeader
import com.railson.recipes.feature.recipedetail.components.PreviewRecipes
import com.railson.recipes.feature.recipedetail.components.ScaledFrom
import com.railson.recipes.feature.recipedetail.components.StatBoxes
import com.railson.recipes.feature.recipedetail.components.StepRow
import com.railson.recipes.recipes.domain.model.Recipe
import com.railson.recipes.recipes.domain.model.instructionsTime
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
    RecipeDetailLayout(uiState = uiState, onAction = viewModel::onAction, onBack = onBack)
}

@Composable
internal fun RecipeDetailLayout(
    uiState: RecipeDetailUiState,
    onAction: (RecipeDetailAction) -> Unit,
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
            RecipeDetailUiState.Loading -> RecipeDetailLoading(modifier = stateModifier)
            RecipeDetailUiState.NotAvailable -> RecipeDetailNotAvailable(onBack = onBack, modifier = stateModifier)
            is RecipeDetailUiState.Content -> RecipeDetailContent(
                content = uiState,
                onAction = onAction,
                onBack = onBack,
                modifier = stateModifier,
            )
        }
    }
}

/** The hero image, then the content panel (DESIGN.md 3.9, 3.10). The top app bar shows once the image is gone. */
@Composable
internal fun RecipeDetailContent(
    content: RecipeDetailUiState.Content,
    onAction: (RecipeDetailAction) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val recipe = content.recipe
    val listState = rememberLazyListState()
    val scrolledPastHero by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 } }
    val navigationBar = WindowInsets.navigationBars.asPaddingValues()
    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = navigationBar.calculateBottomPadding() + Spacing.xl),
        ) {
            item(key = "hero") { Hero(imageUrl = recipe.imageUrl, onBack = onBack) }
            summary(recipe)
            ingredients(content, onAction)
            instructions(recipe)
        }
        AnimatedVisibility(visible = scrolledPastHero, enter = fadeIn(), exit = fadeOut()) {
            CollapsedTopBar(title = recipe.title, onBack = onBack)
        }
    }
}

private val PanelPadding = Modifier.padding(horizontal = Spacing.lg)

/** Cuisine and title, stat boxes, diet tags and description. */
private fun LazyListScope.summary(recipe: Recipe) {
    item(key = "title") {
        Column(modifier = PanelPadding) {
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
    item(key = "stats") { StatBoxes(recipe = recipe, modifier = PanelPadding.padding(top = Spacing.lg)) }
    if (recipe.dietaryTags.isNotEmpty()) {
        item(key = "tags") {
            DetailDietTags(tags = recipe.dietaryTags, modifier = PanelPadding.padding(top = Spacing.lg))
        }
    }
    if (recipe.description.isNotBlank()) {
        item(key = "description") {
            Description(text = recipe.description, modifier = PanelPadding.padding(top = Spacing.lg))
        }
    }
}

private fun LazyListScope.ingredients(content: RecipeDetailUiState.Content, onAction: (RecipeDetailAction) -> Unit) {
    item(key = "ingredients-header") {
        Column(modifier = PanelPadding.padding(top = Spacing.lg)) {
            HorizontalDivider(thickness = 1.dp, color = RecipesTheme.colors.hairline)
            IngredientsHeader(
                scaledServings = content.scaledServings,
                canRemoveServings = content.canRemoveServings,
                canAddServings = content.canAddServings,
                onAction = onAction,
            )
            if (content.isScaled) {
                ScaledFrom(
                    servings = content.recipe.servings,
                    onReset = { onAction(RecipeDetailAction.ResetServings) },
                )
            }
        }
    }
    items(content.ingredients) { ingredient ->
        IngredientRow(ingredient = ingredient, modifier = PanelPadding)
    }
}

private fun LazyListScope.instructions(recipe: Recipe) {
    if (recipe.instructions.isEmpty()) return
    item(key = "instructions-header") {
        InstructionsHeader(
            stepCount = recipe.instructions.size,
            instructionsTime = recipe.instructionsTime,
            modifier = PanelPadding.padding(top = Spacing.sm),
        )
    }
    items(recipe.instructions, key = { "step-${it.number}" }) { step ->
        StepRow(step = step, modifier = PanelPadding)
    }
}

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
            onAction = {},
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
            onAction = {},
            onBack = {},
        )
    }
}
