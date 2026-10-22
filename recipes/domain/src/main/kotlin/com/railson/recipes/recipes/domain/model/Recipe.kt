package com.railson.recipes.recipes.domain.model

import kotlin.time.Duration

data class Recipe(
    val id: String,
    val title: String,
    val description: String,
    val servings: Int,
    val prepTime: Duration,
    val cookTime: Duration,
    val difficulty: Difficulty?,
    val cuisine: String,
    val rating: Double?,
    val imageUrl: String?,
    val dietaryTags: Set<DietaryTag>,
    val ingredients: List<Ingredient>,
    val instructions: List<Step>,
)

data class Ingredient(
    val name: String,
    val quantity: Double,
    val unit: String?,
)

data class Step(
    val number: Int,
    val description: String,
    val duration: Duration?,
)

enum class Difficulty { Easy, Medium, Hard }

enum class DietaryTag { Vegan, Vegetarian, Pescatarian, Halal, GlutenFree, DairyFree, NutFree, LowCarb }
