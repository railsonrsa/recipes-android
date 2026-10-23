package com.railson.recipes.feature.recipelist.filter

import androidx.annotation.StringRes
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.railson.recipes.core.ui.preview.ThemePreviews
import com.railson.recipes.core.ui.theme.RecipesTheme
import com.railson.recipes.core.ui.theme.Spacing
import com.railson.recipes.feature.recipelist.R
import com.railson.recipes.feature.recipelist.components.DietDisplayOrder
import com.railson.recipes.feature.recipelist.components.label
import com.railson.recipes.feature.recipelist.list.RecipeListAction
import com.railson.recipes.recipes.domain.model.DietaryTag
import com.railson.recipes.recipes.domain.model.Difficulty
import com.railson.recipes.recipes.domain.model.SearchQuery
import com.railson.recipes.recipes.domain.model.ServingsRange
import kotlinx.coroutines.launch

private const val SCRIM_ALPHA_LIGHT = 0.32f
private const val SCRIM_ALPHA_DARK = 0.5f

/** The filter bottom sheet (DESIGN.md 2 and 3.8). "Show N recipes" applies the draft and closes it. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RecipeFilterSheet(
    filters: RecipeFiltersUiState,
    snackbarHostState: SnackbarHostState,
    onAction: (RecipeListAction) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val scrimAlpha = if (isSystemInDarkTheme()) SCRIM_ALPHA_DARK else SCRIM_ALPHA_LIGHT
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        scrimColor = MaterialTheme.colorScheme.scrim.copy(alpha = scrimAlpha),
    ) {
        Box(contentAlignment = Alignment.BottomCenter) {
            RecipeFilterSheetContent(
                filters = filters,
                onAction = onAction,
                onShowRecipes = {
                    onAction(RecipeListAction.ShowRecipes)
                    scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
                },
            )
            SnackbarHost(snackbarHostState)
        }
    }
}

/** The sheet's header, sections and sticky "Show N recipes" button, without the sheet itself. */
@Composable
internal fun RecipeFilterSheetContent(
    filters: RecipeFiltersUiState,
    onAction: (RecipeListAction) -> Unit,
    onShowRecipes: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val draft = filters.draft
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = Spacing.lg, end = Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(stringResource(R.string.recipe_list_filters), style = MaterialTheme.typography.titleLarge)
            TextButton(onClick = { onAction(RecipeListAction.ClearDraftFilters) }) {
                Text(
                    text = stringResource(R.string.recipe_filters_clear_all),
                    color = RecipesTheme.colors.actionText,
                )
            }
        }
        // Only the sections scroll. The button stays at the bottom.
        Column(
            modifier = Modifier
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState()),
        ) {
            FilterSection(R.string.recipe_filters_diet, note = stringResource(R.string.recipe_filters_must_match_all)) {
                DietChips(selected = draft.dietaryTags, onToggle = { onAction(RecipeListAction.ToggleDietaryTag(it)) })
            }
            SectionDivider()
            FilterSection(R.string.recipe_filters_servings) {
                ServingsButtons(
                    selected = draft.servingsRange,
                    onSelect = { onAction(RecipeListAction.ChangeServingsRange(it)) },
                )
            }
            SectionDivider()
            FilterSection(R.string.recipe_filters_difficulty) {
                DifficultyChips(
                    selected = draft.difficulties,
                    onToggle = { onAction(RecipeListAction.ToggleDifficulty(it)) },
                )
            }
        }
        Button(
            onClick = onShowRecipes,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.lg, vertical = Spacing.md),
        ) {
            val count = filters.draftCount
            Text(
                if (count == null) {
                    stringResource(R.string.recipe_filters_show)
                } else {
                    pluralStringResource(R.plurals.recipe_filters_show_count, count, count)
                },
            )
        }
    }
}

@Composable
private fun FilterSection(
    @StringRes title: Int,
    note: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg, vertical = Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                text = stringResource(title),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.weight(1f),
            )
            if (note != null) {
                Text(
                    text = note,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        content()
    }
}

@Composable
private fun SectionDivider() {
    HorizontalDivider(modifier = Modifier.padding(horizontal = Spacing.lg), color = RecipesTheme.colors.hairline)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DietChips(selected: Set<DietaryTag>, onToggle: (DietaryTag) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        DietDisplayOrder.forEach { tag ->
            SelectChip(label = stringResource(tag.label), selected = tag in selected, onClick = { onToggle(tag) })
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DifficultyChips(selected: Set<Difficulty>, onToggle: (Difficulty) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        Difficulty.entries.forEach { difficulty ->
            SelectChip(
                label = stringResource(difficulty.label),
                selected = difficulty in selected,
                onClick = { onToggle(difficulty) },
            )
        }
    }
}

@Composable
private fun SelectChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        leadingIcon = if (selected) {
            {
                Icon(
                    painter = painterResource(R.drawable.ic_check),
                    contentDescription = null,
                    modifier = Modifier.size(FilterChipDefaults.IconSize),
                )
            }
        } else {
            null
        },
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = MaterialTheme.colorScheme.outline,
        ),
    )
}

@Composable
private fun ServingsButtons(selected: ServingsRange, onSelect: (ServingsRange) -> Unit) {
    val ranges = ServingsRange.entries
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        ranges.forEachIndexed { index, range ->
            SegmentedButton(
                selected = range == selected,
                onClick = { onSelect(range) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = ranges.size),
                colors = SegmentedButtonDefaults.colors(
                    activeBorderColor = MaterialTheme.colorScheme.outline,
                    inactiveBorderColor = MaterialTheme.colorScheme.outline,
                ),
            ) {
                Text(stringResource(range.label), maxLines = 1)
            }
        }
    }
}

@get:StringRes
internal val ServingsRange.label: Int
    get() = when (this) {
        ServingsRange.Any -> R.string.recipe_filters_servings_any
        ServingsRange.OneToTwo -> R.string.recipe_filters_servings_1_2
        ServingsRange.ThreeToFour -> R.string.recipe_filters_servings_3_4
        ServingsRange.FiveToSix -> R.string.recipe_filters_servings_5_6
        ServingsRange.SevenPlus -> R.string.recipe_filters_servings_7_plus
    }

@ThemePreviews
@Composable
private fun RecipeFilterSheetContentPreview() {
    RecipesTheme {
        RecipeFilterSheetContent(
            filters = RecipeFiltersUiState(
                draft = SearchQuery(
                    dietaryTags = setOf(DietaryTag.Vegetarian),
                    servingsRange = ServingsRange.ThreeToFour,
                    difficulties = setOf(Difficulty.Easy),
                ),
                draftCount = 2,
            ),
            onAction = {},
            onShowRecipes = {},
        )
    }
}

@ThemePreviews
@Composable
private fun RecipeFilterSheetContentNoCountPreview() {
    RecipesTheme {
        RecipeFilterSheetContent(filters = RecipeFiltersUiState(), onAction = {}, onShowRecipes = {})
    }
}
