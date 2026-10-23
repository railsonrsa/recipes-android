package com.railson.recipes.feature.recipelist.list

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.railson.recipes.core.ui.preview.ThemePreviews
import com.railson.recipes.core.ui.theme.RecipesTheme
import com.railson.recipes.feature.recipelist.R

/** A search with text matches no recipe (DESIGN.md 3.5). "Clear all filters" comes with the filters (#24). */
@Composable
internal fun RecipeListNoResults(modifier: Modifier = Modifier) {
    RecipeListMessage(
        icon = R.drawable.ic_search,
        title = stringResource(R.string.recipe_list_no_results_title),
        body = stringResource(R.string.recipe_list_no_results_body),
        modifier = modifier,
    )
}

@ThemePreviews
@Composable
private fun RecipeListNoResultsPreview() {
    RecipesTheme { RecipeListNoResults() }
}
