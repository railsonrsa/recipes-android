package com.railson.recipes.feature.recipelist.filter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.railson.recipes.core.ui.theme.Spacing
import com.railson.recipes.feature.recipelist.R
import com.railson.recipes.feature.recipelist.components.DietDisplayOrder
import com.railson.recipes.feature.recipelist.components.label
import com.railson.recipes.recipes.domain.model.DietaryTag
import com.railson.recipes.recipes.domain.model.Difficulty

/** The multi-select chips of the sheet (DESIGN.md 2, Diet filter and Difficulty filter). */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun DietChips(selected: Set<DietaryTag>, onToggle: (DietaryTag) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        DietDisplayOrder.forEach { tag ->
            SelectChip(label = stringResource(tag.label), selected = tag in selected, onClick = { onToggle(tag) })
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun DifficultyChips(selected: Set<Difficulty>, onToggle: (Difficulty) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        Difficulty.entries.forEach { difficulty ->
            SelectChip(
                label = stringResource(difficulty.label),
                selected = difficulty in selected,
                onClick = { onToggle(difficulty) },
            )
        }
    }
}

@Composable
private fun SelectChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        leadingIcon = if (selected) {
            {
                Icon(
                    painter = painterResource(R.drawable.ic_check),
                    contentDescription = null,
                    modifier = Modifier.size(FilterChipDefaults.IconSize),
                )
            }
        } else {
            null
        },
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = MaterialTheme.colorScheme.outline,
        ),
    )
}
