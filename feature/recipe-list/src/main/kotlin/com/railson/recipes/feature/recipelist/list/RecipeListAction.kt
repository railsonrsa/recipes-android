package com.railson.recipes.feature.recipelist.list

import com.railson.recipes.feature.recipelist.filter.ActiveFilter
import com.railson.recipes.recipes.domain.model.DietaryTag
import com.railson.recipes.recipes.domain.model.Difficulty
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

    /** In the sheet. Each change counts the draft 300 ms after the last one. */
    data class ToggleDietaryTag(val tag: DietaryTag) : RecipeListAction

    data class ChangeServingsRange(val range: ServingsRange) : RecipeListAction

    data class ToggleDifficulty(val difficulty: Difficulty) : RecipeListAction

    /** "Clear all" in the sheet. Nothing changes on the list until "Show N recipes". */
    data object ClearDraftFilters : RecipeListAction

    /** "Show N recipes": the draft becomes the search. */
    data object ShowRecipes : RecipeListAction

    /** The close icon on an active filter chip. */
    data class RemoveFilter(val filter: ActiveFilter) : RecipeListAction

    /** "Clear all filters" on the no results state. The search text stays. */
    data object ClearAllFilters : RecipeListAction
}
