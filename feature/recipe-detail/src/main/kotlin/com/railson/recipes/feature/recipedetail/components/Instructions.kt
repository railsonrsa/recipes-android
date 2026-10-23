package com.railson.recipes.feature.recipedetail.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.railson.recipes.core.ui.format.formatTime
import com.railson.recipes.core.ui.preview.ThemePreviews
import com.railson.recipes.core.ui.theme.RecipesTheme
import com.railson.recipes.core.ui.theme.Spacing
import com.railson.recipes.feature.recipedetail.R
import com.railson.recipes.recipes.domain.model.Step
import kotlin.time.Duration

/** "Instructions" with "N steps · X" on the right, or only "N steps" when no step has a duration (DESIGN.md 3.10). */
@Composable
internal fun InstructionsHeader(
    stepCount: Int,
    instructionsTime: Duration?,
    modifier: Modifier = Modifier,
) {
    SectionHeader(title = stringResource(R.string.recipe_detail_instructions), modifier = modifier) {
        val steps = pluralStringResource(R.plurals.recipe_detail_steps, stepCount, stepCount)
        Text(
            text = instructionsTime?.let { stringResource(R.string.recipe_detail_steps_time, steps, it.formatTime()) }
                ?: steps,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** The number in a circle, the full description, and the duration chip when the step has one. */
@Composable
internal fun StepRow(
    step: Step,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.padding(vertical = Spacing.md),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(MaterialTheme.colorScheme.surfaceContainer, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = step.number.toString(), style = MaterialTheme.typography.labelLarge)
            }
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                Text(text = step.description, style = MaterialTheme.typography.bodyLarge)
                step.duration?.let { StepDuration(it) }
            }
        }
        HorizontalDivider(thickness = 1.dp, color = RecipesTheme.colors.hairline)
    }
}

/** Shaped like an `AssistChip`, but a `Surface`: the chip is not clickable (DESIGN.md 2, Step duration). */
@Composable
private fun StepDuration(duration: Duration) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            modifier = Modifier
                .heightIn(min = 32.dp)
                .padding(horizontal = Spacing.sm),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_timer),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp),
            )
            Text(text = duration.formatTime(), style = MaterialTheme.typography.labelLarge)
        }
    }
}

@ThemePreviews
@Composable
private fun InstructionsPreview() {
    RecipesTheme {
        Surface(color = MaterialTheme.colorScheme.surfaceContainerLowest) {
            Column(modifier = Modifier.padding(horizontal = Spacing.lg)) {
                val steps = PreviewRecipes.margherita.instructions
                InstructionsHeader(stepCount = steps.size, instructionsTime = steps.firstNotNullOf { it.duration })
                InstructionsHeader(stepCount = 1, instructionsTime = null)
                steps.forEach { StepRow(step = it) }
            }
        }
    }
}
