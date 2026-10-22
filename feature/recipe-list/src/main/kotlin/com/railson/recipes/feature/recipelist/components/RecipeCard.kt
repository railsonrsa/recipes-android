package com.railson.recipes.feature.recipelist.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.railson.recipes.core.ui.components.DifficultyBars
import com.railson.recipes.core.ui.components.RecipeImage
import com.railson.recipes.core.ui.format.formatTime
import com.railson.recipes.core.ui.preview.ThemePreviews
import com.railson.recipes.core.ui.theme.RecipesTheme
import com.railson.recipes.core.ui.theme.Spacing
import com.railson.recipes.feature.recipelist.R
import com.railson.recipes.recipes.domain.model.Difficulty
import com.railson.recipes.recipes.domain.model.Recipe
import java.util.Locale
import com.railson.recipes.core.ui.R as CoreUiR

/** A recipe on the list (DESIGN.md 2 and 3.2): thumbnail, title, metadata, servings and cuisine, tags. */
@Composable
internal fun RecipeCard(
    recipe: Recipe,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
    ) {
        Row(
            modifier = Modifier.padding(Spacing.md),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            RecipeImage(
                url = recipe.imageUrl,
                modifier = Modifier
                    .size(80.dp)
                    .clip(MaterialTheme.shapes.small),
            )
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                Text(
                    text = recipe.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                MetadataLine(recipe)
                ServesLine(servings = recipe.servings, cuisine = recipe.cuisine)
                CardDietTags(tags = recipe.dietaryTags)
            }
        }
    }
}

/** "★ 4.7 · 32 min · Medium". No rating shows "No ratings"; an unknown difficulty is left out. */
@Composable
private fun MetadataLine(recipe: Recipe) {
    val style = MaterialTheme.typography.bodyMedium
    val iconSize = with(LocalDensity.current) { style.lineHeight.toDp() }
    val secondary = MaterialTheme.colorScheme.onSurfaceVariant
    FlowRow(itemVerticalAlignment = Alignment.CenterVertically) {
        val rating = recipe.rating
        if (rating == null) {
            Text(stringResource(R.string.recipe_card_no_ratings), style = style, color = secondary)
        } else {
            IconAndText {
                Icon(
                    painter = painterResource(CoreUiR.drawable.ic_star_filled),
                    contentDescription = null,
                    tint = RecipesTheme.colors.ratingStar,
                    modifier = Modifier.size(iconSize),
                )
                Text(String.format(Locale.ENGLISH, "%.1f", rating), style = style, fontWeight = FontWeight.Medium)
            }
        }
        Separator()
        Text((recipe.prepTime + recipe.cookTime).formatTime(), style = style, color = secondary)
        recipe.difficulty?.let { difficulty ->
            Separator()
            val label = stringResource(difficulty.label)
            IconAndText {
                DifficultyBars(
                    filledBars = difficulty.bars,
                    contentDescription = stringResource(R.string.difficulty_description, label.lowercase()),
                    modifier = Modifier.size(iconSize),
                )
                Text(label, style = style, color = secondary)
            }
        }
    }
}

@Composable
private fun IconAndText(content: @Composable () -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs), verticalAlignment = Alignment.CenterVertically) {
        content()
    }
}

@Composable
private fun Separator(style: TextStyle = MaterialTheme.typography.bodyMedium) {
    Text(
        text = stringResource(R.string.recipe_card_separator),
        style = style,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = Spacing.xs),
    )
}

/** "Serves 4 · Italian". The cuisine is the `tertiary` label, left out when blank. */
@Composable
private fun ServesLine(servings: Int, cuisine: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = stringResource(R.string.recipe_card_serves, servings),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (cuisine.isNotBlank()) {
            Separator(MaterialTheme.typography.bodySmall)
            Text(
                text = cuisine,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.tertiary,
            )
        }
    }
}

@get:StringRes
private val Difficulty.label: Int
    get() = when (this) {
        Difficulty.Easy -> R.string.difficulty_easy
        Difficulty.Medium -> R.string.difficulty_medium
        Difficulty.Hard -> R.string.difficulty_hard
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
private fun RecipeCardPreview() {
    RecipesTheme {
        RecipeCard(recipe = PreviewRecipes.margherita)
    }
}

@ThemePreviews
@Composable
private fun RecipeCardEdgeCasesPreview() {
    RecipesTheme {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            PreviewRecipes.edgeCases.forEach { RecipeCard(recipe = it) }
        }
    }
}
