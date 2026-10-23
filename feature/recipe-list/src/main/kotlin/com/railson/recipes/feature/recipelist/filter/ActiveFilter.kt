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
}

/** The chips in DESIGN.md order: diet, servings, difficulty. */
internal val SearchQuery.activeFilters: List<ActiveFilter>
    get() = buildList {
        dietaryTags.sortedBy(DietDisplayOrder::indexOf).forEach { add(ActiveFilter.Diet(it)) }
        if (servingsRange != ServingsRange.Any) add(ActiveFilter.Servings(servingsRange))
        difficulties.sorted().forEach { add(ActiveFilter.Difficulty(it)) }
    }

internal fun SearchQuery.without(filter: ActiveFilter): SearchQuery = when (filter) {
    is ActiveFilter.Diet -> copy(dietaryTags = dietaryTags - filter.tag)
    is ActiveFilter.Servings -> copy(servingsRange = ServingsRange.Any)
    is ActiveFilter.Difficulty -> copy(difficulties = difficulties - filter.difficulty)
}

/** This search's text with the filters of [other]. */
internal fun SearchQuery.withFiltersOf(other: SearchQuery): SearchQuery = other.copy(text = text)

/** This search's text with no filters. */
internal fun SearchQuery.withoutFilters(): SearchQuery = SearchQuery(text = text)
