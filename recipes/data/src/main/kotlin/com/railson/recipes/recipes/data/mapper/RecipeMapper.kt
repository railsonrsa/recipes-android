package com.railson.recipes.recipes.data.mapper

import com.railson.recipes.recipes.data.local.entity.RecipeEntity
import com.railson.recipes.recipes.domain.model.DietaryTag
import com.railson.recipes.recipes.domain.model.Difficulty
import com.railson.recipes.recipes.domain.model.Ingredient
import com.railson.recipes.recipes.domain.model.Recipe
import com.railson.recipes.recipes.domain.model.Step
import kotlin.time.Duration.Companion.seconds

internal fun RecipeEntity.toDomain(): Recipe = Recipe(
    id = id,
    title = title,
    description = description,
    servings = servings,
    prepTime = prepTimeSeconds.seconds,
    cookTime = cookTimeSeconds.seconds,
    difficulty = when (difficulty) {
        1 -> Difficulty.Easy
        2 -> Difficulty.Medium
        3 -> Difficulty.Hard
        else -> null
    },
    cuisine = cuisine,
    rating = rating,
    imageUrl = imageUrl,
    dietaryTags = dietaryTags.mapNotNull(dietaryTagsByKey::get).toSet(),
    ingredients = ingredients.map { Ingredient(name = it.name, quantity = it.quantity, unit = it.unit) },
    instructions = instructions.sortedBy { it.step }.map {
        Step(number = it.step, description = it.description, duration = it.durationSeconds?.seconds)
    },
)

private val dietaryTagsByKey = mapOf(
    "vegan" to DietaryTag.Vegan,
    "vegetarian" to DietaryTag.Vegetarian,
    "pescatarian" to DietaryTag.Pescatarian,
    "halal" to DietaryTag.Halal,
    "gluten-free" to DietaryTag.GlutenFree,
    "dairy-free" to DietaryTag.DairyFree,
    "nut-free" to DietaryTag.NutFree,
    "low-carb" to DietaryTag.LowCarb,
)
