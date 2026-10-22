@file:Suppress("MagicNumber") // Bar sizes are the DESIGN.md 2 drawable, in its 24 dp grid.

package com.railson.recipes.core.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.railson.recipes.core.ui.preview.ThemePreviews
import com.railson.recipes.core.ui.theme.RecipesTheme

private val BarHeights = listOf(7f, 12f, 18f)
private const val GRID = 24f
private const val BAR_WIDTH = 4f
private const val BAR_GAP = 2f
private const val BAR_RADIUS = 2f

/**
 * The difficulty icon: 3 bars, [filledBars] of them filled (1 Easy, 2 Medium, 3 Hard). DESIGN.md 2.
 * It is 24 dp unless [modifier] sets a size. The bars scale with it.
 */
@Composable
fun DifficultyBars(
    filledBars: Int,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    val filled = MaterialTheme.colorScheme.onSurface
    val empty = MaterialTheme.colorScheme.outlineVariant
    Canvas(
        modifier = modifier
            .size(24.dp)
            .semantics { this.contentDescription = contentDescription },
    ) {
        val unit = size.width / GRID
        val bottom = (GRID + BarHeights.last()) / 2 * unit
        val firstLeft = (GRID - BarHeights.size * BAR_WIDTH - (BarHeights.size - 1) * BAR_GAP) / 2
        BarHeights.forEachIndexed { index, height ->
            drawRoundRect(
                color = if (index < filledBars) filled else empty,
                topLeft = Offset((firstLeft + index * (BAR_WIDTH + BAR_GAP)) * unit, bottom - height * unit),
                size = Size(BAR_WIDTH * unit, height * unit),
                cornerRadius = CornerRadius(BAR_RADIUS * unit),
            )
        }
    }
}

@ThemePreviews
@Composable
private fun DifficultyBarsPreview() {
    RecipesTheme {
        Row {
            for (filledBars in 1..3) DifficultyBars(filledBars = filledBars, contentDescription = "")
        }
    }
}
