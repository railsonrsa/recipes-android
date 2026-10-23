package com.railson.recipes.feature.recipedetail.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.railson.recipes.core.ui.preview.ThemePreviews
import com.railson.recipes.core.ui.theme.RecipesTheme
import com.railson.recipes.core.ui.theme.Spacing
import com.railson.recipes.recipes.domain.model.DietaryTag
import com.railson.recipes.recipes.ui.color
import com.railson.recipes.recipes.ui.label
import com.railson.recipes.recipes.ui.sortedForDisplay

/** Every tag, in display order (DESIGN.md 2, Diet tag (detail)). Not clickable. */
@Composable
internal fun DetailDietTags(
    tags: Set<DietaryTag>,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        tags.sortedForDisplay().forEach { tag ->
            Surface(
                shape = MaterialTheme.shapes.small,
                color = tag.color,
                // Set on purpose: some tag colors match a theme role, and Surface would pick that role's text color.
                contentColor = MaterialTheme.colorScheme.onSurface,
            ) {
                Box(
                    modifier = Modifier
                        .heightIn(min = 32.dp)
                        .padding(horizontal = Spacing.md),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = stringResource(tag.label), style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@ThemePreviews
@Composable
private fun DetailDietTagsPreview() {
    RecipesTheme {
        DetailDietTags(tags = DietaryTag.entries.toSet())
    }
}
