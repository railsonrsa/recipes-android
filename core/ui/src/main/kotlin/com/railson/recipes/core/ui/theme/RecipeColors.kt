@file:Suppress("MagicNumber") // Color values are DESIGN.md tokens.

package com.railson.recipes.core.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** The colors M3 has no role for (DESIGN.md 1.1). */
@Immutable
data class RecipeColors(
    val actionText: Color,
    val hairline: Color,
    val ratingStar: Color,
    val dietTags: DietTagColors,
    val spinner: List<Color>,
)

/** One container color per dietary tag (DESIGN.md 1.2). Features pick the color for each `DietaryTag`. */
@Immutable
data class DietTagColors(
    val vegetarian: Color,
    val vegan: Color,
    val glutenFree: Color,
    val dairyFree: Color,
    val nutFree: Color,
    val lowCarb: Color,
    val halal: Color,
    val pescatarian: Color,
)

private val RatingStar = Color(0xFFFFD91B)

private val Spinner = listOf(
    Color(0xFF4E8DDF),
    Color(0xFFFE6F0C),
    Color(0xFF80C532),
    Color(0xFFA259CC),
)

internal val LightRecipeColors = RecipeColors(
    actionText = Color(0xFFBA4D01),
    hairline = Color(0xFFF0EDE5),
    ratingStar = RatingStar,
    dietTags = DietTagColors(
        vegetarian = Color(0xFFD4ECB8),
        vegan = Color(0xFF9AD15B),
        glutenFree = Color(0xFFFFEE8A),
        dairyFree = Color(0xFFB8D4FF),
        nutFree = Color(0xFFFEC79D),
        lowCarb = Color(0xFFD9BFF5),
        halal = Color(0xFFFEE3CE),
        pescatarian = Color(0xFFC4E6EF),
    ),
    spinner = Spinner,
)

internal val DarkRecipeColors = RecipeColors(
    actionText = Color(0xFFFF9A52),
    hairline = Color(0xFF2F2A23),
    ratingStar = RatingStar,
    dietTags = DietTagColors(
        vegetarian = Color(0xFF34492A),
        vegan = Color(0xFF3E5A1C),
        glutenFree = Color(0xFF4D4413),
        dairyFree = Color(0xFF23395A),
        nutFree = Color(0xFF573A22),
        lowCarb = Color(0xFF3F2F57),
        halal = Color(0xFF553621),
        pescatarian = Color(0xFF1F4550),
    ),
    spinner = Spinner,
)

internal val LocalRecipeColors = staticCompositionLocalOf { LightRecipeColors }
