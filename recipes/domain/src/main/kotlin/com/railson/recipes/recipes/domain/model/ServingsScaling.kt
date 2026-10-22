package com.railson.recipes.recipes.domain.model

/** Every quantity times `scaledServings / servings`, units included (docs/specs/domain.md, Servings scaling). */
fun Recipe.scaledIngredients(scaledServings: Int): List<Ingredient> =
    ingredients.map { it.copy(quantity = it.quantity * scaledServings / servings) }

/** The scaled servings the reader can pick. */
val ScaledServingsRange: IntRange = 1..MAX_SCALED_SERVINGS

private const val MAX_SCALED_SERVINGS = 20
