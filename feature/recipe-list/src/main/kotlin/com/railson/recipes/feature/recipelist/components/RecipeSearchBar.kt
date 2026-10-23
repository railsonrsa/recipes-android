package com.railson.recipes.feature.recipelist.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.railson.recipes.core.ui.preview.ThemePreviews
import com.railson.recipes.core.ui.theme.RecipesTheme
import com.railson.recipes.feature.recipelist.R

private const val DISABLED_ALPHA = 0.38f

/** The search bar (DESIGN.md 2). It never expands: the list under it is the result. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RecipeSearchBar(state: TextFieldState, enabled: Boolean, modifier: Modifier = Modifier) {
    val keyboard = LocalSoftwareKeyboardController.current
    val colors = SearchBarDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)
    DockedSearchBar(
        inputField = {
            SearchBarDefaults.InputField(
                state = state,
                // The search runs while typing. The keyboard's search key only closes the keyboard.
                onSearch = { keyboard?.hide() },
                expanded = false,
                onExpandedChange = {},
                enabled = enabled,
                textStyle = MaterialTheme.typography.bodyLarge,
                placeholder = { Text(stringResource(R.string.recipe_list_search_placeholder)) },
                leadingIcon = { Icon(painterResource(R.drawable.ic_search), contentDescription = null) },
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
            .alpha(if (enabled) 1f else DISABLED_ALPHA)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
    ) {}
}

@ThemePreviews
@Composable
private fun RecipeSearchBarPreview() {
    RecipesTheme { RecipeSearchBar(state = rememberTextFieldState("pao"), enabled = true) }
}

@ThemePreviews
@Composable
private fun RecipeSearchBarEmptyPreview() {
    RecipesTheme { RecipeSearchBar(state = rememberTextFieldState(), enabled = true) }
}

@ThemePreviews
@Composable
private fun RecipeSearchBarDisabledPreview() {
    RecipesTheme { RecipeSearchBar(state = rememberTextFieldState(), enabled = false) }
}
