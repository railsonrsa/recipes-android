@file:Suppress("MagicNumber") // Radii are DESIGN.md tokens.

package com.railson.recipes.core.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

internal val RecipeShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    extraLarge = RoundedCornerShape(28.dp),
)
