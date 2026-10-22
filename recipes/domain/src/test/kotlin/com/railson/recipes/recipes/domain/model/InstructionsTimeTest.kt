package com.railson.recipes.recipes.domain.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class InstructionsTimeTest {

    @Test
    fun `instructions time adds the step durations and skips steps without one`() {
        val recipe = recipe(Step(1, "Boil", 10.minutes), Step(2, "Stir", null), Step(3, "Rest", 90.seconds))

        assertThat(recipe.instructionsTime).isEqualTo(11.minutes + 30.seconds)
    }

    @Test
    fun `instructions time is null when no step has a duration`() {
        val recipe = recipe(Step(1, "Mix", null), Step(2, "Serve", null))

        assertThat(recipe.instructionsTime).isNull()
    }
}

private fun recipe(vararg steps: Step) = Recipe(
    id = "id",
    title = "Title",
    description = "",
    servings = 2,
    prepTime = Duration.ZERO,
    cookTime = Duration.ZERO,
    difficulty = null,
    cuisine = "",
    rating = null,
    imageUrl = null,
    dietaryTags = emptySet(),
    ingredients = emptyList(),
    instructions = steps.toList(),
)
