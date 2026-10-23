package com.railson.recipes.feature.recipedetail.components

import androidx.annotation.StringRes
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.railson.recipes.core.ui.R
import com.railson.recipes.core.ui.preview.ThemePreviews
import com.railson.recipes.core.ui.theme.RecipesTheme
import com.railson.recipes.core.ui.theme.Spacing
import com.railson.recipes.recipes.domain.model.DietaryTag

/** The most specific first (DESIGN.md 1.2). */
private val DisplayOrder = listOf(
    DietaryTag.Vegan,
    DietaryTag.Vegetarian,
    DietaryTag.Pescatarian,
    DietaryTag.Halal,
    DietaryTag.GlutenFree,
    DietaryTag.DairyFree,
    DietaryTag.NutFree,
    DietaryTag.LowCarb,
)

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
        tags.sortedBy(DisplayOrder::indexOf).forEach { tag ->
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

@get:StringRes
private val DietaryTag.label: Int
    get() = when (this) {
        DietaryTag.Vegan -> R.string.diet_vegan
        DietaryTag.Vegetarian -> R.string.diet_vegetarian
        DietaryTag.Pescatarian -> R.string.diet_pescatarian
        DietaryTag.Halal -> R.string.diet_halal
        DietaryTag.GlutenFree -> R.string.diet_gluten_free
        DietaryTag.DairyFree -> R.string.diet_dairy_free
        DietaryTag.NutFree -> R.string.diet_nut_free
        DietaryTag.LowCarb -> R.string.diet_low_carb
    }

private val DietaryTag.color: Color
    @Composable
    get() = with(RecipesTheme.colors.dietTags) {
        when (this@color) {
            DietaryTag.Vegan -> vegan
            DietaryTag.Vegetarian -> vegetarian
            DietaryTag.Pescatarian -> pescatarian
            DietaryTag.Halal -> halal
            DietaryTag.GlutenFree -> glutenFree
            DietaryTag.DairyFree -> dairyFree
            DietaryTag.NutFree -> nutFree
            DietaryTag.LowCarb -> lowCarb
        }
    }

@ThemePreviews
@Composable
private fun DetailDietTagsPreview() {
    RecipesTheme {
        DetailDietTags(tags = DietaryTag.entries.toSet())
    }
}
