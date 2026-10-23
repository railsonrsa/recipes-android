package com.railson.recipes.feature.recipelist.filter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.railson.recipes.core.ui.preview.ThemePreviews
import com.railson.recipes.core.ui.theme.RecipesTheme
import com.railson.recipes.core.ui.theme.Spacing
import com.railson.recipes.feature.recipelist.R
import com.railson.recipes.feature.recipelist.components.color
import com.railson.recipes.feature.recipelist.components.label
import com.railson.recipes.recipes.domain.model.DietaryTag
import com.railson.recipes.recipes.domain.model.Difficulty
import com.railson.recipes.recipes.domain.model.SearchQuery
import com.railson.recipes.recipes.domain.model.ServingsRange

/** The applied filters under the search bar (DESIGN.md 2, Active filter). Tapping one removes it. */
@Composable
internal fun ActiveFilterChips(
    filters: List<ActiveFilter>,
    onRemove: (ActiveFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = Spacing.lg),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        items(filters, key = { it.toString() }) { filter ->
            val diet = (filter as? ActiveFilter.Diet)?.tag
            InputChip(
                selected = false,
                onClick = { onRemove(filter) },
                label = { Text(filter.text()) },
                trailingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_close),
                        contentDescription = stringResource(R.string.recipe_list_remove_filter),
                        modifier = Modifier.size(InputChipDefaults.IconSize),
                    )
                },
                colors = if (diet != null) {
                    InputChipDefaults.inputChipColors(containerColor = diet.color)
                } else {
                    InputChipDefaults.inputChipColors()
                },
                // A diet chip is its color. The others keep the outlined look.
                border = if (diet != null) {
                    null
                } else {
                    InputChipDefaults.inputChipBorder(
                        enabled = true,
                        selected = false,
                        borderColor = MaterialTheme.colorScheme.outline,
                    )
                },
            )
        }
    }
}

@Composable
private fun ActiveFilter.text(): String = when (this) {
    is ActiveFilter.Diet -> stringResource(tag.label)
    is ActiveFilter.Servings -> stringResource(R.string.recipe_list_filter_servings, stringResource(range.label))
    is ActiveFilter.Difficulty -> stringResource(difficulty.label)
}

@ThemePreviews
@Composable
private fun ActiveFilterChipsPreview() {
    RecipesTheme {
        ActiveFilterChips(
            filters = SearchQuery(
                dietaryTags = setOf(DietaryTag.Vegan, DietaryTag.GlutenFree),
                servingsRange = ServingsRange.ThreeToFour,
                difficulties = setOf(Difficulty.Easy),
            ).activeFilters,
            onRemove = {},
        )
    }
}
