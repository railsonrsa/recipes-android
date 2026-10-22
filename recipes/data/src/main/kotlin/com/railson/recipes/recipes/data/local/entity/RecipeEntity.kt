package com.railson.recipes.recipes.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.railson.recipes.recipes.data.remote.IngredientDto
import com.railson.recipes.recipes.data.remote.StepDto

/** A recipe as the server sent it. The rules that turn it into a `Recipe` run when it is read. */
@Entity(tableName = "recipes")
internal data class RecipeEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val servings: Int,
    val prepTimeSeconds: Long,
    val cookTimeSeconds: Long,
    val difficulty: Int?,
    val cuisine: String,
    val rating: Double?,
    val imageUrl: String?,
    val dietaryTags: List<String>,
    val ingredients: List<IngredientDto>,
    val instructions: List<StepDto>,
)
