package com.railson.recipes.feature.recipelist.filter

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Color
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
            ActiveFilterChip(filter = filter, onRemove = { onRemove(filter) })
        }
    }
}

/** One filter as an `InputChip`, the same on the list and in the sheet's ingredient section. */
@Composable
internal fun ActiveFilterChip(filter: ActiveFilter, onRemove: () -> Unit, modifier: Modifier = Modifier) {
    val look = filter.look()
    InputChip(
        selected = false,
        onClick = onRemove,
        label = { Text(filter.text()) },
        modifier = modifier,
        leadingIcon = look.icon?.let { icon ->
            {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                    modifier = Modifier.size(InputChipDefaults.IconSize),
                )
            }
        },
        trailingIcon = {
            Icon(
                painter = painterResource(R.drawable.ic_close),
                contentDescription = stringResource(R.string.recipe_list_remove_filter),
                modifier = Modifier.size(InputChipDefaults.IconSize),
            )
        },
        colors = InputChipDefaults.inputChipColors(containerColor = look.container),
        border = look.border,
    )
}

private data class ChipLook(val container: Color, val border: BorderStroke?, @DrawableRes val icon: Int? = null)

/** DESIGN.md 2: diet and included ingredients wear a diet color, steps the secondary container, the rest a border. */
@Composable
private fun ActiveFilter.look(): ChipLook {
    val colors = MaterialTheme.colorScheme
    val outlined = InputChipDefaults.inputChipBorder(enabled = true, selected = false, borderColor = colors.outline)
    return when (this) {
        is ActiveFilter.Diet -> ChipLook(container = tag.color, border = null)
        is ActiveFilter.IncludedIngredient -> ChipLook(container = DietaryTag.Vegetarian.color, border = null)
        is ActiveFilter.ExcludedIngredient ->
            ChipLook(container = colors.surfaceContainerLowest, border = outlined, icon = R.drawable.ic_block)
        is ActiveFilter.InstructionSearch ->
            ChipLook(container = colors.secondaryContainer, border = null, icon = R.drawable.ic_format_list_numbered)
        is ActiveFilter.Servings, is ActiveFilter.Difficulty ->
            ChipLook(container = Color.Transparent, border = outlined)
    }
}

@Composable
private fun ActiveFilter.text(): String = when (this) {
    is ActiveFilter.Diet -> stringResource(tag.label)
    is ActiveFilter.Servings -> stringResource(R.string.recipe_list_filter_servings, stringResource(range.label))
    is ActiveFilter.Difficulty -> stringResource(difficulty.label)
    is ActiveFilter.IncludedIngredient -> word
    is ActiveFilter.ExcludedIngredient -> stringResource(R.string.recipe_list_filter_excluded, word)
    is ActiveFilter.InstructionSearch -> stringResource(R.string.recipe_list_filter_steps, text)
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
                includedIngredients = setOf("Coconut milk"),
                excludedIngredients = setOf("garlic"),
                instructionSearch = "oven",
            ).activeFilters,
            onRemove = {},
        )
    }
}
