package com.railson.recipes.recipes.ui

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.railson.recipes.core.ui.theme.RecipesTheme
import com.railson.recipes.recipes.domain.model.DietaryTag

/** The most specific first (DESIGN.md 1.2). */
val DietDisplayOrder = listOf(
    DietaryTag.Vegan,
    DietaryTag.Vegetarian,
    DietaryTag.Pescatarian,
    DietaryTag.Halal,
    DietaryTag.GlutenFree,
    DietaryTag.DairyFree,
    DietaryTag.NutFree,
    DietaryTag.LowCarb,
)

fun Collection<DietaryTag>.sortedForDisplay(): List<DietaryTag> = sortedBy(DietDisplayOrder::indexOf)

@get:StringRes
val DietaryTag.label: Int
    get() = when (this) {
        DietaryTag.Vegan -> R.string.diet_vegan
        DietaryTag.Vegetarian -> R.string.diet_vegetarian
        DietaryTag.Pescatarian -> R.string.diet_pescatarian
        DietaryTag.Halal -> R.string.diet_halal
        DietaryTag.GlutenFree -> R.string.diet_gluten_free
        DietaryTag.DairyFree -> R.string.diet_dairy_free
        DietaryTag.NutFree -> R.string.diet_nut_free
        DietaryTag.LowCarb -> R.string.diet_low_carb
    }

/** The tag's container color from the theme (DESIGN.md 1.2). */
val DietaryTag.color: Color
    @Composable
    get() = with(RecipesTheme.colors.dietTags) {
        when (this@color) {
            DietaryTag.Vegan -> vegan
            DietaryTag.Vegetarian -> vegetarian
            DietaryTag.Pescatarian -> pescatarian
            DietaryTag.Halal -> halal
            DietaryTag.GlutenFree -> glutenFree
            DietaryTag.DairyFree -> dairyFree
            DietaryTag.NutFree -> nutFree
            DietaryTag.LowCarb -> lowCarb
        }
    }
