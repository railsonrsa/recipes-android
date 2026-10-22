package com.railson.recipes.feature.recipedetail.detail

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.railson.recipes.feature.recipedetail.testing.FakeRecipeRepository
import com.railson.recipes.feature.recipedetail.testing.MainDispatcherRule
import com.railson.recipes.recipes.domain.model.Ingredient
import com.railson.recipes.recipes.domain.model.Recipe
import com.railson.recipes.recipes.domain.usecase.ObserveRecipeUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.time.Duration

class RecipeDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeRecipeRepository()

    private fun viewModel(id: String = CURRY_ID) =
        RecipeDetailViewModel(id = id, observeRecipe = ObserveRecipeUseCase(repository))

    private fun save(recipe: Recipe) {
        repository.saved.value = mapOf(recipe.id to recipe)
    }

    @Test
    fun `a recipe that was never saved is not available`() = runTest {
        save(curry())
        val viewModel = viewModel(id = "never-saved")

        viewModel.uiState.test {
            assertThat(awaitItem()).isEqualTo(RecipeDetailUiState.NotAvailable)
        }
    }

    @Test
    fun `a saved recipe opens at its own servings`() = runTest {
        save(curry(servings = 4))
        val viewModel = viewModel()

        viewModel.uiState.test {
            val content = awaitItem() as RecipeDetailUiState.Content
            assertThat(content.scaledServings).isEqualTo(4)
            assertThat(content.isScaled).isFalse()
            assertThat(content.ingredients).isEqualTo(curry().ingredients)
        }
    }

    @Test
    fun `more servings scale every quantity up`() = runTest {
        save(curry(servings = 4))
        val viewModel = viewModel()

        viewModel.uiState.test {
            skipItems(1)
            viewModel.onAction(RecipeDetailAction.MoreServings)

            val content = awaitItem() as RecipeDetailUiState.Content
            assertThat(content.scaledServings).isEqualTo(5)
            assertThat(content.isScaled).isTrue()
            assertThat(content.ingredients.map { it.quantity }).containsExactly(500.0, 1.25).inOrder()
        }
    }

    @Test
    fun `fewer servings scale every quantity down`() = runTest {
        save(curry(servings = 4))
        val viewModel = viewModel()

        viewModel.uiState.test {
            skipItems(1)
            viewModel.onAction(RecipeDetailAction.FewerServings)

            val content = awaitItem() as RecipeDetailUiState.Content
            assertThat(content.scaledServings).isEqualTo(3)
            assertThat(content.ingredients.map { it.quantity }).containsExactly(300.0, 0.75).inOrder()
        }
    }

    @Test
    fun `servings stop at 20`() = runTest {
        save(curry(servings = 19))
        val viewModel = viewModel()

        viewModel.uiState.test {
            skipItems(1)
            viewModel.onAction(RecipeDetailAction.MoreServings)
            val atMost = awaitItem() as RecipeDetailUiState.Content
            viewModel.onAction(RecipeDetailAction.MoreServings)

            assertThat(atMost.scaledServings).isEqualTo(20)
            assertThat(atMost.canAddServings).isFalse()
            expectNoEvents()
        }
    }

    @Test
    fun `servings stop at 1`() = runTest {
        save(curry(servings = 2))
        val viewModel = viewModel()

        viewModel.uiState.test {
            skipItems(1)
            viewModel.onAction(RecipeDetailAction.FewerServings)
            val atLeast = awaitItem() as RecipeDetailUiState.Content
            viewModel.onAction(RecipeDetailAction.FewerServings)

            assertThat(atLeast.scaledServings).isEqualTo(1)
            assertThat(atLeast.canRemoveServings).isFalse()
            expectNoEvents()
        }
    }

    @Test
    fun `reset goes back to the servings as written`() = runTest {
        save(curry(servings = 4))
        val viewModel = viewModel()

        viewModel.uiState.test {
            skipItems(1)
            viewModel.onAction(RecipeDetailAction.MoreServings)
            skipItems(1)
            viewModel.onAction(RecipeDetailAction.ResetServings)

            val content = awaitItem() as RecipeDetailUiState.Content
            assertThat(content.scaledServings).isEqualTo(4)
            assertThat(content.isScaled).isFalse()
            assertThat(content.ingredients).isEqualTo(curry().ingredients)
        }
    }
}

private const val CURRY_ID = "curry"

private fun curry(servings: Int = 4) = Recipe(
    id = CURRY_ID,
    title = "Chickpea Coconut Curry",
    description = "",
    servings = servings,
    prepTime = Duration.ZERO,
    cookTime = Duration.ZERO,
    difficulty = null,
    cuisine = "",
    rating = null,
    imageUrl = null,
    dietaryTags = emptySet(),
    ingredients = listOf(Ingredient("Chickpeas", 400.0, "g"), Ingredient("Salt", 1.0, "pinch")),
    instructions = emptyList(),
)
