package com.railson.recipes.feature.recipedetail.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.railson.recipes.core.ui.components.RecipeImage
import com.railson.recipes.core.ui.preview.ThemePreviews
import com.railson.recipes.core.ui.theme.RecipesTheme
import com.railson.recipes.core.ui.theme.Spacing
import com.railson.recipes.feature.recipedetail.R

/** 232 dp, under the status bar, with the panel's rounded top over its bottom 28 dp. */
@Composable
internal fun Hero(imageUrl: String?, onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(232.dp),
    ) {
        RecipeImage(url = imageUrl, modifier = Modifier.fillMaxSize())
        BackButton(
            onClick = onBack,
            modifier = Modifier
                .statusBarsPadding()
                .padding(Spacing.sm),
        )
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerLowest,
            shape = RoundedCornerShape(topStart = PanelRadius, topEnd = PanelRadius),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(PanelRadius),
        ) {}
    }
}

@Composable
internal fun BackButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(
        onClick = onClick,
        modifier = modifier,
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
        shape = CircleShape,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_arrow_back),
            contentDescription = stringResource(R.string.recipe_detail_back),
            modifier = Modifier.size(24.dp),
        )
    }
}

private val PanelRadius = 28.dp

/** Back and the title on one line (DESIGN.md 2, Screen title). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CollapsedTopBar(title: String, onBack: () -> Unit) {
    TopAppBar(
        title = { Text(text = title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_back),
                    contentDescription = stringResource(R.string.recipe_detail_back),
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        ),
    )
}

@ThemePreviews
@Composable
private fun HeroPreview() {
    RecipesTheme { Hero(imageUrl = null, onBack = {}) }
}

@ThemePreviews
@Composable
private fun CollapsedTopBarPreview() {
    RecipesTheme { CollapsedTopBar(title = PreviewRecipes.edgeCases.title, onBack = {}) }
}
