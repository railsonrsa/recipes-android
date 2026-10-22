package com.railson.recipes.feature.recipelist.list

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.railson.recipes.core.ui.preview.ThemePreviews
import com.railson.recipes.core.ui.theme.RecipesTheme
import com.railson.recipes.feature.recipelist.R

@Composable
internal fun RecipeListScreen(topBarActions: @Composable RowScope.() -> Unit) {
    RecipeListLayout(topBarActions = topBarActions)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RecipeListLayout(
    topBarActions: @Composable RowScope.() -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.recipe_list_title)) },
                actions = topBarActions,
            )
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding))
    }
}

@ThemePreviews
@Composable
private fun RecipeListLayoutPreview() {
    RecipesTheme {
        RecipeListLayout(topBarActions = {})
    }
}
