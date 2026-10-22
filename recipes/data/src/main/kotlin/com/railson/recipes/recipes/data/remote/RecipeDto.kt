package com.railson.recipes.recipes.data.remote

import kotlinx.serialization.Serializable

@Serializable
internal data class RecipePageDto(
    val data: List<RecipeDto>,
    val pagination: PaginationDto,
)

@Serializable
internal data class PaginationDto(
    val page: Int,
    val pageSize: Int,
    val totalItems: Int,
    val totalPages: Int,
    val hasNext: Boolean,
    val nextPage: Int?,
    val previousPage: Int?,
)

@Serializable
internal data class RecipeDto(
    val id: String,
    val title: String,
    val description: String,
    val servings: Int,
    val prepTimeSeconds: Long,
    val cookTimeSeconds: Long,
    val difficulty: Int?,
    val cuisine: String,
    val rating: Double?,
    val imageUrl: String?,
    val dietaryAttributes: List<String>,
    val ingredients: List<IngredientDto>,
    val instructions: List<StepDto>,
)

@Serializable
internal data class IngredientDto(
    val name: String,
    val quantity: Double,
    val unit: String?,
)

@Serializable
internal data class StepDto(
    val step: Int,
    val description: String,
    val durationSeconds: Long?,
)

@Serializable
internal data class ErrorDto(
    val code: String,
    val message: String,
)
