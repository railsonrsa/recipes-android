package com.railson.recipes.recipes.domain.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import kotlin.time.Duration

class ServingsScalingTest {

    @Test
    fun `doubling the servings doubles every quantity`() {
        val recipe = recipe(servings = 4, Ingredient("Flour", 500.0, "g"), Ingredient("Eggs", 2.0, null))

        val scaled = recipe.scaledIngredients(scaledServings = 8)

        assertThat(scaled).containsExactly(Ingredient("Flour", 1000.0, "g"), Ingredient("Eggs", 4.0, null)).inOrder()
    }

    @Test
    fun `fewer servings give a fraction of each quantity`() {
        val recipe = recipe(servings = 4, Ingredient("Lime", 1.0, null))

        val scaled = recipe.scaledIngredients(scaledServings = 2)

        assertThat(scaled.single().quantity).isEqualTo(0.5)
    }

    @Test
    fun `a pinch scales like any other unit`() {
        val recipe = recipe(servings = 2, Ingredient("Salt", 1.0, "pinch"))

        val scaled = recipe.scaledIngredients(scaledServings = 6)

        assertThat(scaled.single()).isEqualTo(Ingredient("Salt", 3.0, "pinch"))
    }

    @Test
    fun `the servings as written keep every quantity`() {
        val recipe = recipe(servings = 3, Ingredient("Rice", 200.0, "g"))

        val scaled = recipe.scaledIngredients(scaledServings = 3)

        assertThat(scaled.single().quantity).isEqualTo(200.0)
    }
}

private fun recipe(servings: Int, vararg ingredients: Ingredient) = Recipe(
    id = "id",
    title = "Title",
    description = "",
    servings = servings,
    prepTime = Duration.ZERO,
    cookTime = Duration.ZERO,
    difficulty = null,
    cuisine = "",
    rating = null,
    imageUrl = null,
    dietaryTags = emptySet(),
    ingredients = ingredients.toList(),
    instructions = emptyList(),
)
