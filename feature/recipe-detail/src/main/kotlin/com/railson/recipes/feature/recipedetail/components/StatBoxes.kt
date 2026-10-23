package com.railson.recipes.feature.recipedetail.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.railson.recipes.core.ui.components.DifficultyBars
import com.railson.recipes.core.ui.format.formatTime
import com.railson.recipes.core.ui.preview.ThemePreviews
import com.railson.recipes.core.ui.theme.RecipesTheme
import com.railson.recipes.core.ui.theme.Spacing
import com.railson.recipes.feature.recipedetail.R
import com.railson.recipes.recipes.domain.model.Difficulty
import com.railson.recipes.recipes.domain.model.Recipe
import java.util.Locale
import com.railson.recipes.core.ui.R as CoreUiR

/** Prep, Cook, Level and Rating, 4 equal boxes (DESIGN.md 2, Stat box). */
@Composable
internal fun StatBoxes(
    recipe: Recipe,
    modifier: Modifier = Modifier,
) {
    val unknown = stringResource(R.string.recipe_detail_unknown)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        val boxModifier = Modifier
            .weight(1f)
            .fillMaxHeight()
        StatBox(
            value = recipe.prepTime.formatTime(),
            label = stringResource(R.string.recipe_detail_prep),
            modifier = boxModifier,
        ) { StatIcon(R.drawable.ic_timer) }
        StatBox(
            value = if (recipe.cookTime.isPositive()) {
                recipe.cookTime.formatTime()
            } else {
                stringResource(R.string.recipe_detail_no_cook_time)
            },
            label = stringResource(R.string.recipe_detail_cook),
            modifier = boxModifier,
        ) { StatIcon(R.drawable.ic_local_fire_department) }
        val difficulty = recipe.difficulty
        val level = difficulty?.let { stringResource(it.label) }
        StatBox(
            value = level ?: unknown,
            label = stringResource(R.string.recipe_detail_level),
            modifier = boxModifier,
        ) {
            DifficultyBars(
                filledBars = difficulty?.bars ?: 0,
                contentDescription = level?.let {
                    stringResource(CoreUiR.string.difficulty_description, it.lowercase())
                }.orEmpty(),
            )
        }
        StatBox(
            value = recipe.rating?.let { String.format(Locale.ENGLISH, "%.1f", it) } ?: unknown,
            label = stringResource(R.string.recipe_detail_rating),
            modifier = boxModifier,
        ) { StatIcon(R.drawable.ic_star) }
    }
}

@Composable
private fun StatBox(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
) {
    OutlinedCard(
        modifier = modifier,
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Spacing.md, horizontal = Spacing.xs),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            icon()
            Text(text = value, style = MaterialTheme.typography.titleSmall, textAlign = TextAlign.Center)
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun StatIcon(@DrawableRes icon: Int) {
    Icon(painter = painterResource(icon), contentDescription = null, modifier = Modifier.size(24.dp))
}

private val Difficulty.label: Int
    get() = when (this) {
        Difficulty.Easy -> CoreUiR.string.difficulty_easy
        Difficulty.Medium -> CoreUiR.string.difficulty_medium
        Difficulty.Hard -> CoreUiR.string.difficulty_hard
    }

@Suppress("MagicNumber") // Filled bars per difficulty, DESIGN.md 2.
private val Difficulty.bars: Int
    get() = when (this) {
        Difficulty.Easy -> 1
        Difficulty.Medium -> 2
        Difficulty.Hard -> 3
    }

@ThemePreviews
@Composable
private fun StatBoxesPreview() {
    RecipesTheme {
        StatBoxes(recipe = PreviewRecipes.margherita)
    }
}

@ThemePreviews
@Composable
private fun StatBoxesUnknownPreview() {
    RecipesTheme {
        StatBoxes(recipe = PreviewRecipes.edgeCases)
    }
}
