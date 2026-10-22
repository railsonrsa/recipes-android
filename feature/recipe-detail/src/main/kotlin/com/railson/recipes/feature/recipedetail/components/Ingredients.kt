package com.railson.recipes.feature.recipedetail.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.railson.recipes.core.ui.preview.ThemePreviews
import com.railson.recipes.core.ui.theme.RecipesTheme
import com.railson.recipes.core.ui.theme.Spacing
import com.railson.recipes.feature.recipedetail.R
import com.railson.recipes.feature.recipedetail.detail.RecipeDetailAction
import com.railson.recipes.feature.recipedetail.detail.formatQuantity
import com.railson.recipes.recipes.domain.model.Ingredient

/** "Ingredients" and the servings stepper on the right (DESIGN.md 3.9). */
@Composable
internal fun IngredientsHeader(
    scaledServings: Int,
    canRemoveServings: Boolean,
    canAddServings: Boolean,
    onAction: (RecipeDetailAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    SectionHeader(title = stringResource(R.string.recipe_detail_ingredients), modifier = modifier) {
        Surface(
            shape = CircleShape,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            color = MaterialTheme.colorScheme.surfaceContainerLowest,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                StepperButton(
                    icon = R.drawable.ic_remove,
                    description = stringResource(R.string.recipe_detail_fewer_servings),
                    enabled = canRemoveServings,
                    onClick = { onAction(RecipeDetailAction.FewerServings) },
                )
                Text(
                    text = stringResource(R.string.recipe_detail_serves, scaledServings),
                    style = MaterialTheme.typography.labelLarge,
                )
                StepperButton(
                    icon = R.drawable.ic_add,
                    description = stringResource(R.string.recipe_detail_more_servings),
                    enabled = canAddServings,
                    onClick = { onAction(RecipeDetailAction.MoreServings) },
                )
            }
        }
    }
}

/** "Scaled from 4 servings · Reset", only while the reader changed the servings. */
@Composable
internal fun ScaledFrom(
    servings: Int,
    onReset: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = pluralStringResource(R.plurals.recipe_detail_scaled_from, servings, servings),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        TextButton(onClick = onReset) {
            Text(stringResource(R.string.recipe_detail_reset), color = RecipesTheme.colors.actionText)
        }
    }
}

/** Name on the left, scaled quantity on the right, a hairline below. */
@Composable
internal fun IngredientRow(
    ingredient: Ingredient,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.heightIn(min = 48.dp),
            horizontalArrangement = Arrangement.spacedBy(Spacing.lg),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = ingredient.name, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            Text(
                text = formatQuantity(ingredient.quantity, ingredient.unit),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.End,
            )
        }
        HorizontalDivider(thickness = 1.dp, color = RecipesTheme.colors.hairline)
    }
}

/** A 56 dp header row: the title in `tertiary`, and [trailing] on the right. */
@Composable
internal fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    trailing: @Composable () -> Unit,
) {
    Row(
        modifier = modifier.heightIn(min = 56.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.weight(1f),
        )
        trailing()
    }
}

@Composable
private fun StepperButton(
    @DrawableRes icon: Int,
    description: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    IconButton(onClick = onClick, enabled = enabled) {
        Icon(
            painter = painterResource(icon),
            contentDescription = description,
            tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
            modifier = Modifier.size(20.dp),
        )
    }
}

@ThemePreviews
@Composable
private fun IngredientsPreview() {
    RecipesTheme {
        Surface(color = MaterialTheme.colorScheme.surfaceContainerLowest) {
            Column(modifier = Modifier.padding(horizontal = Spacing.lg)) {
                IngredientsHeader(
                    scaledServings = 20,
                    canRemoveServings = true,
                    canAddServings = false,
                    onAction = {},
                )
                ScaledFrom(servings = 4, onReset = {})
                PreviewRecipes.margherita.ingredients.forEach { IngredientRow(ingredient = it) }
            }
        }
    }
}
