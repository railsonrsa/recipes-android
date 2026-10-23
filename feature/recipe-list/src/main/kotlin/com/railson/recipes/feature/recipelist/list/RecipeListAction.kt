package com.railson.recipes.feature.recipelist.list

import com.railson.recipes.feature.recipelist.filter.ActiveFilter
import com.railson.recipes.recipes.domain.model.DietaryTag
import com.railson.recipes.recipes.domain.model.Difficulty
import com.railson.recipes.recipes.domain.model.IngredientSide
import com.railson.recipes.recipes.domain.model.ServingsRange

/** What the reader does on the list screen. */
internal sealed interface RecipeListAction {
    /** Pull-to-refresh, "Try again", "Refresh" and every "Retry". */
    data object Refresh : RecipeListAction

    /** Types in the search bar. The search waits 300 ms after the last change. */
    data class ChangeSearchText(val text: String) : RecipeListAction

    /** Taps a recipe card. */
    data class OpenRecipe(val id: String) : RecipeListAction

    /** Taps the filter button. The sheet starts from the applied filters. */
    data object OpenFilters : RecipeListAction

    /** An edit of the draft in the sheet. Each one counts the draft 300 ms after the last one. */
    sealed interface DraftChange : RecipeListAction

    data class ToggleDietaryTag(val tag: DietaryTag) : DraftChange

    data class ChangeServingsRange(val range: ServingsRange) : DraftChange

    data class ToggleDifficulty(val difficulty: Difficulty) : DraftChange

    /** The "+" button. A word on the other side moves, and a snackbar says so. */
    data class AddIngredient(val word: String, val side: IngredientSide) : DraftChange

    /** The close icon on an ingredient chip in the sheet. */
    data class RemoveIngredient(val word: String) : DraftChange

    data class ChangeInstructionSearch(val text: String) : DraftChange

    /** "Clear all" in the sheet. Nothing changes on the list until "Show N recipes". */
    data object ClearDraftFilters : DraftChange

    /** "Show N recipes": the draft becomes the search. */
    data object ShowRecipes : RecipeListAction

    /** The close icon on an active filter chip. */
    data class RemoveFilter(val filter: ActiveFilter) : RecipeListAction

    /** "Clear all filters" on the no results state. The search text stays. */
    data object ClearAllFilters : RecipeListAction
}
