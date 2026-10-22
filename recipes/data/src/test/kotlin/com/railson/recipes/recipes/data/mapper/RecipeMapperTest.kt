package com.railson.recipes.recipes.data.mapper

import com.google.common.truth.Truth.assertThat
import com.railson.recipes.recipes.data.local.entity.RecipeEntity
import com.railson.recipes.recipes.data.remote.IngredientDto
import com.railson.recipes.recipes.data.remote.StepDto
import com.railson.recipes.recipes.domain.model.DietaryTag
import com.railson.recipes.recipes.domain.model.Difficulty
import org.junit.Test
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class RecipeMapperTest {

    @Test
    fun `difficulty 1, 2 and 3 become Easy, Medium and Hard`() {
        val difficulties = listOf(1, 2, 3).map { entity(difficulty = it).toDomain().difficulty }

        assertThat(difficulties).containsExactly(Difficulty.Easy, Difficulty.Medium, Difficulty.Hard).inOrder()
    }

    @Test
    fun `any other difficulty is unknown`() {
        val difficulties = listOf(0, 4, null).map { entity(difficulty = it).toDomain().difficulty }

        assertThat(difficulties).containsExactly(null, null, null)
    }

    @Test
    fun `known diet keys become dietary tags and unknown keys are dropped`() {
        val recipe = entity(dietaryTags = listOf("vegan", "gluten-free", "keto", "dairy-free", "nut-free")).toDomain()

        assertThat(recipe.dietaryTags)
            .containsExactly(DietaryTag.Vegan, DietaryTag.GlutenFree, DietaryTag.DairyFree, DietaryTag.NutFree)
    }

    @Test
    fun `every diet key in the spec has a dietary tag`() {
        val keys = listOf(
            "vegan", "vegetarian", "pescatarian", "halal", "gluten-free", "dairy-free", "nut-free", "low-carb",
        )

        val recipe = entity(dietaryTags = keys).toDomain()

        assertThat(recipe.dietaryTags).containsExactlyElementsIn(DietaryTag.entries)
    }

    @Test
    fun `prep and cook seconds become durations`() {
        val recipe = entity(prepTimeSeconds = 900, cookTimeSeconds = 5400).toDomain()

        assertThat(recipe.prepTime).isEqualTo(15.minutes)
        assertThat(recipe.cookTime).isEqualTo(90.minutes)
    }

    @Test
    fun `step seconds become durations and a missing duration stays missing`() {
        val steps = listOf(
            StepDto(step = 1, description = "Stir", durationSeconds = 30),
            StepDto(step = 2, description = "Serve", durationSeconds = null),
        )

        val recipe = entity(instructions = steps).toDomain()

        assertThat(recipe.instructions.map { it.duration }).containsExactly(30.seconds, null).inOrder()
    }
}

private fun entity(
    difficulty: Int? = 1,
    dietaryTags: List<String> = emptyList(),
    prepTimeSeconds: Long = 0,
    cookTimeSeconds: Long = 0,
    instructions: List<StepDto> = emptyList(),
) = RecipeEntity(
    id = "id",
    title = "Title",
    description = "Description",
    servings = 4,
    prepTimeSeconds = prepTimeSeconds,
    cookTimeSeconds = cookTimeSeconds,
    difficulty = difficulty,
    cuisine = "Italian",
    rating = null,
    imageUrl = null,
    dietaryTags = dietaryTags,
    ingredients = listOf(IngredientDto(name = "Eggs", quantity = 2.0, unit = null)),
    instructions = instructions,
)
