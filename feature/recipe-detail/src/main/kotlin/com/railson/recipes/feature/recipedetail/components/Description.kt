package com.railson.recipes.feature.recipedetail.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.railson.recipes.core.ui.preview.ThemePreviews
import com.railson.recipes.core.ui.theme.RecipesTheme
import com.railson.recipes.feature.recipedetail.R

private const val COLLAPSED_LINES = 3

/** 3 lines, then "Read more", which shows the rest in place (DESIGN.md 4). */
@Composable
internal fun Description(
    text: String,
    modifier: Modifier = Modifier,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    var overflows by rememberSaveable { mutableStateOf(false) }
    Column(modifier = modifier) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = if (expanded) Int.MAX_VALUE else COLLAPSED_LINES,
            overflow = TextOverflow.Ellipsis,
            onTextLayout = { if (!expanded) overflows = it.hasVisualOverflow },
        )
        if (overflows && !expanded) {
            TextButton(onClick = { expanded = true }) {
                Text(stringResource(R.string.recipe_detail_read_more), color = RecipesTheme.colors.actionText)
            }
        }
    }
}

@ThemePreviews
@Composable
private fun DescriptionPreview() {
    RecipesTheme {
        Description(text = PreviewRecipes.edgeCases.description)
    }
}
