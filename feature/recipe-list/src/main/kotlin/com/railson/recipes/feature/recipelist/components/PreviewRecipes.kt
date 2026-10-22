package com.railson.recipes.feature.recipelist.components

import com.railson.recipes.recipes.domain.model.DietaryTag
import com.railson.recipes.recipes.domain.model.Difficulty
import com.railson.recipes.recipes.domain.model.Recipe
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

/** Recipes from the asset for previews, with the edge cases from docs/specs/test-data.md. */
internal object PreviewRecipes {

    val margherita = recipe(
        id = "margherita",
        title = "Classic Margherita Pizza",
        cookMinutes = 12,
        difficulty = Difficulty.Medium,
        cuisine = "Italian",
        rating = 4.7,
        tags = setOf(DietaryTag.Vegetarian),
    )

    /** No image, and 5 tags: 2 + "+3". */
    private val lemonChicken = recipe(
        id = "lemon-chicken",
        title = "Grilled Lemon Herb Chicken",
        cookMinutes = 16,
        difficulty = Difficulty.Easy,
        cuisine = "Mediterranean",
        rating = 4.3,
        imageUrl = null,
        tags = setOf(
            DietaryTag.GlutenFree,
            DietaryTag.DairyFree,
            DietaryTag.NutFree,
            DietaryTag.LowCarb,
            DietaryTag.Halal,
        ),
    )

    private val risottoNoRating = recipe(
        id = "risotto",
        title = "Creamy Mushroom Risotto",
        cookMinutes = 35,
        difficulty = Difficulty.Hard,
        cuisine = "Italian",
        rating = null,
        tags = setOf(DietaryTag.Vegetarian, DietaryTag.GlutenFree, DietaryTag.NutFree),
    )

    /** No tags, and an unknown difficulty (none in the asset). */
    private val tacosNoTags = recipe(
        id = "tacos",
        title = "Beef Tacos with Fresh Salsa",
        cookMinutes = 15,
        difficulty = null,
        cuisine = "Mexican",
        rating = 4.1,
        tags = emptySet(),
    )

    private val lambLongTitle = recipe(
        id = "lamb",
        title = "Slow-Roasted Moroccan-Spiced Lamb Shoulder with Preserved Lemon, Green Olives and " +
            "Fresh Herb Couscous for a Big Family Sunday Gathering",
        cookMinutes = 4.hours.inWholeMinutes.toInt(),
        servings = 8,
        difficulty = Difficulty.Hard,
        cuisine = "Moroccan",
        rating = 4.4,
        tags = setOf(DietaryTag.DairyFree, DietaryTag.NutFree, DietaryTag.Halal),
    )

    val edgeCases = listOf(lemonChicken, risottoNoRating, tacosNoTags, lambLongTitle)

    val all = listOf(margherita) + edgeCases

    @Suppress("LongParameterList") // A preview builder: named arguments read like the asset.
    private fun recipe(
        id: String,
        title: String,
        cookMinutes: Int,
        difficulty: Difficulty?,
        cuisine: String,
        rating: Double?,
        tags: Set<DietaryTag>,
        servings: Int = 4,
        imageUrl: String? = "https://picsum.photos/seed/$id/600/400",
    ) = Recipe(
        id = id,
        title = title,
        description = "",
        servings = servings,
        prepTime = 15.minutes,
        cookTime = cookMinutes.minutes,
        difficulty = difficulty,
        cuisine = cuisine,
        rating = rating,
        imageUrl = imageUrl,
        dietaryTags = tags,
        ingredients = emptyList(),
        instructions = emptyList(),
    )
}
