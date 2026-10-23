package com.railson.recipes.feature.recipelist.filter

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import com.railson.recipes.core.ui.preview.ThemePreviews
import com.railson.recipes.core.ui.theme.RecipesTheme
import com.railson.recipes.core.ui.theme.Spacing
import com.railson.recipes.feature.recipelist.R
import com.railson.recipes.recipes.domain.model.IngredientSide

/**
 * The Ingredients section (DESIGN.md 2 and 3.8): the Must have / Must not have switch picks where the next
 * word goes. The switch and the typed word belong to this sheet only; they are not filters.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun IngredientFilter(
    included: Set<String>,
    excluded: Set<String>,
    onAdd: (String, IngredientSide) -> Unit,
    onRemove: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var side by rememberSaveable { mutableStateOf(IngredientSide.Included) }
    val word = rememberTextFieldState()
    val add = {
        onAdd(word.text.toString(), side)
        word.clearText()
    }
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        IngredientSideButtons(selected = side, onSelect = { side = it })
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            OutlinedTextField(
                state = word,
                modifier = Modifier.weight(1f),
                placeholder = { Text(stringResource(side.placeholder)) },
                leadingIcon = { Icon(painterResource(R.drawable.ic_search), contentDescription = null) },
                lineLimits = TextFieldLineLimits.SingleLine,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                onKeyboardAction = { add() },
            )
            FilledTonalIconButton(
                onClick = add,
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_add),
                    contentDescription = stringResource(R.string.recipe_filters_add_ingredient),
                )
            }
        }
        if (included.isNotEmpty() || excluded.isNotEmpty()) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                included.forEach {
                    ActiveFilterChip(ActiveFilter.IncludedIngredient(it), onRemove = { onRemove(it) })
                }
                excluded.forEach {
                    ActiveFilterChip(ActiveFilter.ExcludedIngredient(it), onRemove = { onRemove(it) })
                }
            }
        }
    }
}

@Composable
private fun IngredientSideButtons(selected: IngredientSide, onSelect: (IngredientSide) -> Unit) {
    val sides = IngredientSide.entries
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        sides.forEachIndexed { index, side ->
            SegmentedButton(
                selected = side == selected,
                onClick = { onSelect(side) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = sides.size),
                colors = SegmentedButtonDefaults.colors(
                    activeBorderColor = MaterialTheme.colorScheme.outline,
                    inactiveBorderColor = MaterialTheme.colorScheme.outline,
                ),
            ) {
                Text(stringResource(side.label), maxLines = 1)
            }
        }
    }
}

/** The Instructions section: one field, no add button. The screen owns its text. */
@Composable
internal fun InstructionSearchField(state: TextFieldState, modifier: Modifier = Modifier) {
    OutlinedTextField(
        state = state,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text(stringResource(R.string.recipe_filters_instructions_placeholder)) },
        leadingIcon = { Icon(painterResource(R.drawable.ic_format_list_numbered), contentDescription = null) },
        trailingIcon = if (state.text.isNotEmpty()) {
            {
                IconButton(onClick = { state.clearText() }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_close),
                        contentDescription = stringResource(R.string.recipe_filters_clear_instructions),
                    )
                }
            }
        } else {
            null
        },
        lineLimits = TextFieldLineLimits.SingleLine,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
    )
}

@get:StringRes
private val IngredientSide.label: Int
    get() = when (this) {
        IngredientSide.Included -> R.string.recipe_filters_must_have
        IngredientSide.Excluded -> R.string.recipe_filters_must_not_have
    }

@get:StringRes
private val IngredientSide.placeholder: Int
    get() = when (this) {
        IngredientSide.Included -> R.string.recipe_filters_ingredient_placeholder
        IngredientSide.Excluded -> R.string.recipe_filters_avoid_placeholder
    }

@ThemePreviews
@Composable
private fun IngredientFilterPreview() {
    RecipesTheme {
        IngredientFilter(
            included = setOf("Coconut milk"),
            excluded = setOf("garlic"),
            onAdd = { _, _ -> },
            onRemove = {},
        )
    }
}

@ThemePreviews
@Composable
private fun InstructionSearchFieldPreview() {
    RecipesTheme {
        InstructionSearchField(state = rememberTextFieldState("oven"))
    }
}
