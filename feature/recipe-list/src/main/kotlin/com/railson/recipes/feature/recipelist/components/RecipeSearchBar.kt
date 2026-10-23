package com.railson.recipes.feature.recipelist.components

import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.railson.recipes.core.ui.preview.ThemePreviews
import com.railson.recipes.core.ui.theme.RecipesTheme
import com.railson.recipes.core.ui.theme.Spacing
import com.railson.recipes.feature.recipelist.R

private const val DISABLED_ALPHA = 0.38f

/**
 * The search bar (DESIGN.md 2). It never expands: the list under it is the result.
 * While the reader types, it stays enabled even when [enabled] is false: disabling a focused
 * field moves the focus away and drops what comes next.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RecipeSearchBar(
    state: TextFieldState,
    enabled: Boolean,
    activeFilterCount: Int,
    onFiltersClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val keyboard = LocalSoftwareKeyboardController.current
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()
    val fieldEnabled = enabled || focused
    val colors = SearchBarDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)
    DockedSearchBar(
        inputField = {
            SearchBarDefaults.InputField(
                state = state,
                // The search runs while typing. The keyboard's search key only closes the keyboard.
                onSearch = { keyboard?.hide() },
                expanded = false,
                onExpandedChange = {},
                enabled = fieldEnabled,
                interactionSource = interactionSource,
                textStyle = MaterialTheme.typography.bodyLarge,
                placeholder = { Text(stringResource(R.string.recipe_list_search_placeholder)) },
                leadingIcon = { Icon(painterResource(R.drawable.ic_search), contentDescription = null) },
                trailingIcon = {
                    FiltersButton(
                        activeFilterCount = activeFilterCount,
                        enabled = fieldEnabled,
                        onClick = onFiltersClick,
                    )
                },
                // The field paints its own container, so it takes the bar's colors too.
                colors = colors.inputFieldColors,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        expanded = false,
        onExpandedChange = {},
        shape = CircleShape,
        colors = colors,
        tonalElevation = 0.dp,
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (fieldEnabled) 1f else DISABLED_ALPHA)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
    ) {}
}

/** The `tune` button in a 40 dp circle, with the active filter count as a badge. */
@Composable
private fun FiltersButton(activeFilterCount: Int, enabled: Boolean, onClick: () -> Unit) {
    val description = if (activeFilterCount == 0) {
        stringResource(R.string.recipe_list_filters)
    } else {
        pluralStringResource(R.plurals.recipe_list_filters_active, activeFilterCount, activeFilterCount)
    }
    IconButton(
        onClick = onClick,
        enabled = enabled,
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
        modifier = Modifier
            .padding(end = Spacing.sm)
            .semantics { contentDescription = description },
    ) {
        BadgedBox(
            badge = {
                if (activeFilterCount > 0) {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ) {
                        Text(text = activeFilterCount.toString(), style = MaterialTheme.typography.labelSmall)
                    }
                }
            },
        ) {
            Icon(painterResource(R.drawable.ic_tune), contentDescription = null)
        }
    }
}

@ThemePreviews
@Composable
private fun RecipeSearchBarPreview() {
    RecipesTheme {
        RecipeSearchBar(
            state = rememberTextFieldState("pao"),
            enabled = true,
            activeFilterCount = 2,
            onFiltersClick = {},
        )
    }
}

@ThemePreviews
@Composable
private fun RecipeSearchBarEmptyPreview() {
    RecipesTheme {
        RecipeSearchBar(state = rememberTextFieldState(), enabled = true, activeFilterCount = 0, onFiltersClick = {})
    }
}

@ThemePreviews
@Composable
private fun RecipeSearchBarDisabledPreview() {
    RecipesTheme {
        RecipeSearchBar(state = rememberTextFieldState(), enabled = false, activeFilterCount = 0, onFiltersClick = {})
    }
}
