package com.railson.recipes.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.railson.recipes.core.ui.R
import com.railson.recipes.core.ui.preview.ThemePreviews
import com.railson.recipes.core.ui.theme.RecipesTheme

/**
 * A recipe photo over the placeholder (DESIGN.md 4). The placeholder shows when there is no URL,
 * while the image loads, and when it fails, offline included.
 */
@Composable
fun RecipeImage(
    url: String?,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceContainer),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_restaurant),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline,
        )
        if (url != null) {
            AsyncImage(
                model = url,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize(),
            )
        }
    }
}

@ThemePreviews
@Composable
private fun RecipeImagePreview() {
    RecipesTheme {
        RecipeImage(
            url = null,
            modifier = Modifier
                .size(80.dp)
                .clip(MaterialTheme.shapes.small),
        )
    }
}
