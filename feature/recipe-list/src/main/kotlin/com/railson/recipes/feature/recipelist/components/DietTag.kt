package com.railson.recipes.feature.recipelist.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.railson.recipes.core.ui.preview.ThemePreviews
import com.railson.recipes.core.ui.theme.RecipesTheme
import com.railson.recipes.core.ui.theme.Spacing
import com.railson.recipes.feature.recipelist.R
import com.railson.recipes.recipes.domain.model.DietaryTag
import com.railson.recipes.recipes.ui.color
import com.railson.recipes.recipes.ui.label
import com.railson.recipes.recipes.ui.sortedForDisplay

private const val CARD_TAG_COUNT = 2

/** The first 2 tags in display order, then "+N" for the rest. Nothing when there are no tags. */
@Composable
internal fun CardDietTags(
    tags: Set<DietaryTag>,
    modifier: Modifier = Modifier,
) {
    val ordered = tags.sortedForDisplay()
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        ordered.take(CARD_TAG_COUNT).forEach { tag ->
            CardTag(text = stringResource(tag.label), color = tag.color)
        }
        if (ordered.size > CARD_TAG_COUNT) {
            CardTag(
                text = stringResource(R.string.recipe_card_more_tags, ordered.size - CARD_TAG_COUNT),
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                textColor = MaterialTheme.colorScheme.onSurfaceVariant,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            )
        }
    }
}

@Composable
private fun CardTag(
    text: String,
    color: Color,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    border: BorderStroke? = null,
) {
    Surface(shape = MaterialTheme.shapes.small, color = color, contentColor = textColor, border = border) {
        Box(
            modifier = Modifier
                .heightIn(min = 24.dp)
                .padding(horizontal = Spacing.sm),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = text, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@ThemePreviews
@Composable
private fun CardDietTagsPreview() {
    RecipesTheme {
        CardDietTags(tags = DietaryTag.entries.toSet())
    }
}
