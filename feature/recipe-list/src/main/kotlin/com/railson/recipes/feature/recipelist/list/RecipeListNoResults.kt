package com.railson.recipes.feature.recipelist.list

import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.railson.recipes.core.ui.preview.ThemePreviews
import com.railson.recipes.core.ui.theme.RecipesTheme
import com.railson.recipes.feature.recipelist.R

/**
 * A search matches no recipe (DESIGN.md 3.5). "Clear all filters" shows only when filters are on:
 * it keeps the search text, which the reader clears in the search bar.
 */
@Composable
internal fun RecipeListNoResults(hasFilters: Boolean, onClearAllFilters: () -> Unit, modifier: Modifier = Modifier) {
    RecipeListMessage(
        icon = R.drawable.ic_search,
        title = stringResource(R.string.recipe_list_no_results_title),
        body = stringResource(R.string.recipe_list_no_results_body),
        modifier = modifier,
        action = if (hasFilters) {
            {
                TextButton(onClick = onClearAllFilters) {
                    Text(
                        text = stringResource(R.string.recipe_list_clear_all_filters),
                        color = RecipesTheme.colors.actionText,
                    )
                }
            }
        } else {
            null
        },
    )
}

@ThemePreviews
@Composable
private fun RecipeListNoResultsPreview() {
    RecipesTheme { RecipeListNoResults(hasFilters = true, onClearAllFilters = {}) }
}

@ThemePreviews
@Composable
private fun RecipeListNoResultsTextOnlyPreview() {
    RecipesTheme { RecipeListNoResults(hasFilters = false, onClearAllFilters = {}) }
}
