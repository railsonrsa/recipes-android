package com.railson.recipes.core.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.railson.recipes.core.ui.preview.ThemePreviews
import com.railson.recipes.core.ui.theme.RecipesTheme

private const val COLOR_CYCLE_MILLIS = 3_600

/**
 * The spinner from DESIGN.md 2: the arc color moves through the `spinner` colors, one full cycle
 * in about 3.6 s, over a `hairline` track. Set the size with [modifier].
 */
@Composable
fun RecipesProgressIndicator(
    strokeWidth: Dp,
    modifier: Modifier = Modifier,
) {
    val colors = RecipesTheme.colors.spinner
    val position by rememberInfiniteTransition(label = "spinner colors").animateFloat(
        initialValue = 0f,
        targetValue = colors.size.toFloat(),
        animationSpec = infiniteRepeatable(tween(COLOR_CYCLE_MILLIS, easing = LinearEasing)),
        label = "spinner color",
    )
    val index = position.toInt() % colors.size
    CircularProgressIndicator(
        modifier = modifier,
        color = lerp(colors[index], colors[(index + 1) % colors.size], position - position.toInt()),
        strokeWidth = strokeWidth,
        trackColor = RecipesTheme.colors.hairline,
    )
}

@ThemePreviews
@Composable
private fun RecipesProgressIndicatorPreview() {
    RecipesTheme {
        RecipesProgressIndicator(strokeWidth = 4.dp, modifier = Modifier.size(32.dp))
    }
}
