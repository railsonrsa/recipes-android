@file:Suppress("MagicNumber") // Values copied from the asset.

package com.railson.recipes.feature.recipedetail.components

import com.railson.recipes.recipes.domain.model.DietaryTag
import com.railson.recipes.recipes.domain.model.Difficulty
import com.railson.recipes.recipes.domain.model.Ingredient
import com.railson.recipes.recipes.domain.model.Recipe
import com.railson.recipes.recipes.domain.model.Step
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/** Recipes from the asset for previews, with the edge cases from docs/specs/test-data.md. */
internal object PreviewRecipes {

    val margherita = Recipe(
        id = "margherita",
        title = "Classic Margherita Pizza",
        description = "A simple Neapolitan pizza with tomato, fresh mozzarella and basil on a thin, crisp base.",
        servings = 4,
        prepTime = 20.minutes,
        cookTime = 12.minutes,
        difficulty = Difficulty.Medium,
        cuisine = "Italian",
        rating = 4.7,
        imageUrl = "https://picsum.photos/seed/margherita/600/400",
        dietaryTags = setOf(DietaryTag.Vegetarian),
        ingredients = listOf(
            Ingredient("Pizza dough", 500.0, "g"),
            Ingredient("Tomato sauce", 0.5, "cup"),
            Ingredient("Fresh basil leaves", 12.0, null),
            Ingredient("Salt", 1.0, "pinch"),
        ),
        instructions = listOf(
            Step(1, "Heat the oven to its highest setting with a tray inside.", 30.minutes),
            Step(2, "Stretch the dough into a thin round and spread the sauce over it.", null),
            Step(3, "Bake until the crust is golden and the cheese bubbles.", 90.seconds),
        ),
    )

    /** No image, no rating, unknown difficulty, no cook time, a long title and description, no step times. */
    val edgeCases = margherita.copy(
        id = "lamb",
        title = "Slow-Roasted Moroccan-Spiced Lamb Shoulder with Preserved Lemon, Green Olives and " +
            "Fresh Herb Couscous for a Big Family Sunday Gathering",
        description = List(4) { margherita.description }.joinToString(" "),
        cookTime = Duration.ZERO,
        difficulty = null,
        rating = null,
        imageUrl = null,
        dietaryTags = DietaryTag.entries.toSet(),
        instructions = margherita.instructions.map { it.copy(duration = null) },
    )
}
