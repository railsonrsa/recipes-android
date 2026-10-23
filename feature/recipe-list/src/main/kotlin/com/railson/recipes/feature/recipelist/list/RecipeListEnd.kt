package com.railson.recipes.feature.recipelist.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.railson.recipes.core.ui.components.RecipesProgressIndicator
import com.railson.recipes.core.ui.preview.ThemePreviews
import com.railson.recipes.core.ui.theme.RecipesTheme
import com.railson.recipes.core.ui.theme.Spacing
import com.railson.recipes.feature.recipelist.R

/** One skeleton card, then "Loading more recipes…" (DESIGN.md 3.3). */
@Composable
internal fun RecipeListLoadingMore(modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        SkeletonCard()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Spacing.md),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RecipesProgressIndicator(strokeWidth = 3.dp, modifier = Modifier.size(24.dp))
            Text(
                text = stringResource(R.string.recipe_list_loading_more),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** "Couldn't load more" + "Retry". The cards above stay (docs/specs/data.md, Screen states). */
@Composable
internal fun RecipeListLoadMoreFailed(onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.xs),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.recipe_list_load_more_failed),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        TextButton(onClick = onRetry) {
            Text(stringResource(R.string.recipe_list_retry), color = RecipesTheme.colors.actionText)
        }
    }
}

/** "That's all 10 recipes" between two lines (DESIGN.md 3.4). */
@Composable
internal fun RecipeListAllLoaded(totalItems: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.lg),
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = RecipesTheme.colors.hairline)
        Text(
            text = pluralStringResource(R.plurals.recipe_list_all_loaded, totalItems, totalItems),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        HorizontalDivider(modifier = Modifier.weight(1f), color = RecipesTheme.colors.hairline)
    }
}

/** The recipe card's layout with `hairline` blocks in place of the image and the text lines. */
@Composable
private fun SkeletonCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
    ) {
        Row(
            modifier = Modifier.padding(Spacing.md),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            SkeletonBlock(Modifier.size(80.dp))
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                SkeletonBlock(Modifier.size(width = 180.dp, height = 16.dp))
                SkeletonBlock(Modifier.size(width = 140.dp, height = 12.dp))
                SkeletonBlock(Modifier.size(width = 100.dp, height = 12.dp))
                SkeletonTags(height = 24.dp)
            }
        }
    }
}

@Composable
private fun SkeletonTags(height: Dp) {
    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        SkeletonBlock(Modifier.size(width = 64.dp, height = height))
        SkeletonBlock(Modifier.size(width = 48.dp, height = height))
    }
}

@Composable
private fun SkeletonBlock(modifier: Modifier) {
    Box(modifier.background(RecipesTheme.colors.hairline, MaterialTheme.shapes.small))
}

@ThemePreviews
@Composable
private fun RecipeListEndPreview() {
    RecipesTheme {
        Column(Modifier.background(MaterialTheme.colorScheme.surface).padding(Spacing.lg)) {
            RecipeListLoadingMore()
            RecipeListLoadMoreFailed(onRetry = {})
            RecipeListAllLoaded(totalItems = 10)
        }
    }
}
