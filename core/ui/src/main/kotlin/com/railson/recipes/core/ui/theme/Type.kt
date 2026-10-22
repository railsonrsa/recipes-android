@file:Suppress("MagicNumber") // Sizes are DESIGN.md tokens.
@file:OptIn(ExperimentalTextApi::class) // FontVariation, for the variable Figtree font.

package com.railson.recipes.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.railson.recipes.core.ui.R

// figtree.ttf is a variable font, so each weight sets the "wght" axis.
private val Figtree = FontFamily(
    Font(
        R.font.figtree,
        weight = FontWeight.Normal,
        variationSettings = FontVariation.Settings(FontVariation.weight(400)),
    ),
    Font(
        R.font.figtree,
        weight = FontWeight.Medium,
        variationSettings = FontVariation.Settings(FontVariation.weight(500)),
    ),
)

private val YoungSerif = FontFamily(Font(R.font.young_serif, FontWeight.Normal))

private fun style(family: FontFamily, size: Int, lineHeight: Int, weight: FontWeight, tracking: Double) =
    TextStyle(
        fontFamily = family,
        fontSize = size.sp,
        lineHeight = lineHeight.sp,
        fontWeight = weight,
        letterSpacing = tracking.sp,
    )

internal val RecipeTypography = Typography(
    headlineSmall = style(YoungSerif, 24, 32, FontWeight.Normal, 0.0),
    titleLarge = style(YoungSerif, 22, 28, FontWeight.Normal, 0.0),
    titleMedium = style(Figtree, 16, 24, FontWeight.Medium, 0.15),
    titleSmall = style(Figtree, 14, 20, FontWeight.Medium, 0.1),
    bodyLarge = style(Figtree, 16, 24, FontWeight.Normal, 0.5),
    bodyMedium = style(Figtree, 14, 20, FontWeight.Normal, 0.25),
    bodySmall = style(Figtree, 12, 16, FontWeight.Normal, 0.4),
    labelLarge = style(Figtree, 14, 20, FontWeight.Medium, 0.1),
    labelMedium = style(Figtree, 12, 16, FontWeight.Medium, 0.5),
    labelSmall = style(Figtree, 11, 16, FontWeight.Medium, 0.5),
)
