package com.railson.recipes.feature.recipelist.filter

import com.railson.recipes.feature.recipelist.components.DietDisplayOrder
import com.railson.recipes.recipes.domain.model.DietaryTag
import com.railson.recipes.recipes.domain.model.SearchQuery
import com.railson.recipes.recipes.domain.model.ServingsRange
import com.railson.recipes.recipes.domain.model.Difficulty as RecipeDifficulty

/** One condition the reader turned on, as a chip under the search bar (DESIGN.md 2, Active filter). */
internal sealed interface ActiveFilter {
    data class Diet(val tag: DietaryTag) : ActiveFilter

    data class Servings(val range: ServingsRange) : ActiveFilter

    data class Difficulty(val difficulty: RecipeDifficulty) : ActiveFilter

    data class IncludedIngredient(val word: String) : ActiveFilter

    data class ExcludedIngredient(val word: String) : ActiveFilter

    data class InstructionSearch(val text: String) : ActiveFilter
}

/** The chips in DESIGN.md order: diet, servings, difficulty, included, excluded, instructions. */
internal val SearchQuery.activeFilters: List<ActiveFilter>
    get() = buildList {
        dietaryTags.sortedBy(DietDisplayOrder::indexOf).forEach { add(ActiveFilter.Diet(it)) }
        if (servingsRange != ServingsRange.Any) add(ActiveFilter.Servings(servingsRange))
        difficulties.sorted().forEach { add(ActiveFilter.Difficulty(it)) }
        includedIngredients.forEach { add(ActiveFilter.IncludedIngredient(it)) }
        excludedIngredients.forEach { add(ActiveFilter.ExcludedIngredient(it)) }
        if (instructionSearch.isNotBlank()) add(ActiveFilter.InstructionSearch(instructionSearch))
    }

internal fun SearchQuery.without(filter: ActiveFilter): SearchQuery = when (filter) {
    is ActiveFilter.Diet -> copy(dietaryTags = dietaryTags - filter.tag)
    is ActiveFilter.Servings -> copy(servingsRange = ServingsRange.Any)
    is ActiveFilter.Difficulty -> copy(difficulties = difficulties - filter.difficulty)
    is ActiveFilter.IncludedIngredient -> withoutIngredient(filter.word)
    is ActiveFilter.ExcludedIngredient -> withoutIngredient(filter.word)
    is ActiveFilter.InstructionSearch -> copy(instructionSearch = "")
}

/** This search's text with no filters. */
internal fun SearchQuery.withoutFilters(): SearchQuery = SearchQuery(text = text)
