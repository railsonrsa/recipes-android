package com.railson.recipes.feature.recipelist.list

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.railson.recipes.core.ui.components.RecipesProgressIndicator
import com.railson.recipes.core.ui.preview.ThemePreviews
import com.railson.recipes.core.ui.theme.RecipesTheme
import com.railson.recipes.core.ui.theme.Spacing
import com.railson.recipes.feature.recipelist.R

/** First load: the spinner and "Loading recipes…" (DESIGN.md 3.1). */
@Composable
internal fun RecipeListLoading(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(Spacing.lg, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        RecipesProgressIndicator(strokeWidth = 4.dp, modifier = Modifier.size(32.dp))
        Text(
            text = stringResource(R.string.recipe_list_loading),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** Nothing saved and the call failed (DESIGN.md 3.6). */
@Composable
internal fun RecipeListError(onTryAgain: () -> Unit, modifier: Modifier = Modifier) {
    RecipeListMessage(
        icon = R.drawable.ic_error,
        title = stringResource(R.string.recipe_list_error_title),
        body = stringResource(R.string.recipe_list_error_body),
        modifier = modifier,
    ) {
        Button(onClick = onTryAgain) { Text(stringResource(R.string.recipe_list_try_again)) }
    }
}

/** Nothing saved for this search and no connection (docs/specs/data.md, Screen states). */
@Composable
internal fun RecipeListOffline(onRetry: () -> Unit, modifier: Modifier = Modifier) {
    RecipeListMessage(
        icon = R.drawable.ic_cloud_off,
        title = stringResource(R.string.recipe_list_offline_title),
        body = stringResource(R.string.recipe_list_offline_body),
        modifier = modifier,
    ) {
        Button(onClick = onRetry) { Text(stringResource(R.string.recipe_list_retry)) }
    }
}

/** The server has no recipes at all (DESIGN.md 3.7). */
@Composable
internal fun RecipeListEmptyCatalogue(onRefresh: () -> Unit, modifier: Modifier = Modifier) {
    RecipeListMessage(
        icon = com.railson.recipes.core.ui.R.drawable.ic_restaurant,
        title = stringResource(R.string.recipe_list_empty_catalogue_title),
        body = stringResource(R.string.recipe_list_empty_catalogue_body),
        modifier = modifier,
    ) {
        OutlinedButton(onClick = onRefresh, contentPadding = ButtonDefaults.ButtonWithIconContentPadding) {
            Icon(
                painter = painterResource(R.drawable.ic_refresh),
                contentDescription = null,
                modifier = Modifier.size(ButtonDefaults.IconSize),
            )
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text(stringResource(R.string.recipe_list_refresh))
        }
    }
}

@Composable
private fun RecipeListMessage(
    @DrawableRes icon: Int,
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    action: @Composable () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = Spacing.lg),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(MaterialTheme.colorScheme.surfaceContainer, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(36.dp),
            )
        }
        Spacer(Modifier.height(Spacing.lg))
        Text(text = title, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
        Spacer(Modifier.height(Spacing.sm))
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(Spacing.xl))
        action()
    }
}

@ThemePreviews
@Composable
private fun RecipeListLoadingPreview() {
    RecipesTheme { RecipeListLoading() }
}

@ThemePreviews
@Composable
private fun RecipeListErrorPreview() {
    RecipesTheme { RecipeListError(onTryAgain = {}) }
}

@ThemePreviews
@Composable
private fun RecipeListOfflinePreview() {
    RecipesTheme { RecipeListOffline(onRetry = {}) }
}

@ThemePreviews
@Composable
private fun RecipeListEmptyCataloguePreview() {
    RecipesTheme { RecipeListEmptyCatalogue(onRefresh = {}) }
}
