package com.railson.recipes.recipes.domain.model

/** The search text plus the filters (docs/specs/domain.md, Search). The empty search is the whole catalog. */
data class SearchQuery(
    val text: String = "",
    val dietaryTags: Set<DietaryTag> = emptySet(),
    val servingsRange: ServingsRange = ServingsRange.Any,
    val difficulties: Set<Difficulty> = emptySet(),
    val includedIngredients: Set<String> = emptySet(),
    val excludedIngredients: Set<String> = emptySet(),
    val instructionSearch: String = "",
) {
    /** The filter badge: one for each condition the reader turned on. The search text does not count. */
    val activeFilterCount: Int
        get() = dietaryTags.size + difficulties.size + includedIngredients.size + excludedIngredients.size +
            (if (servingsRange == ServingsRange.Any) 0 else 1) +
            if (instructionSearch.isBlank()) 0 else 1

    /** The side [word] is on, in any case, or `null` when it is on neither. */
    fun sideOf(word: String): IngredientSide? {
        val trimmed = word.trim()
        return when {
            includedIngredients.any { it.equals(trimmed, ignoreCase = true) } -> IngredientSide.Included
            excludedIngredients.any { it.equals(trimmed, ignoreCase = true) } -> IngredientSide.Excluded
            else -> null
        }
    }

    /** Adds [word] to [side]. A word can't be on both sides, so one on the other side moves. Blank is ignored. */
    fun withIngredient(word: String, side: IngredientSide): SearchQuery {
        val trimmed = word.trim()
        if (trimmed.isEmpty() || sideOf(trimmed) == side) return this
        val rest = withoutIngredient(trimmed)
        return when (side) {
            IngredientSide.Included -> rest.copy(includedIngredients = rest.includedIngredients + trimmed)
            IngredientSide.Excluded -> rest.copy(excludedIngredients = rest.excludedIngredients + trimmed)
        }
    }

    /** Takes [word] off whichever side it is on. */
    fun withoutIngredient(word: String): SearchQuery {
        val trimmed = word.trim()
        return copy(
            includedIngredients = includedIngredients.filterNot { it.equals(trimmed, ignoreCase = true) }.toSet(),
            excludedIngredients = excludedIngredients.filterNot { it.equals(trimmed, ignoreCase = true) }.toSet(),
        )
    }

    companion object {
        val Catalog = SearchQuery()
    }
}

/** Where an ingredient word goes: Must have or Must not have. */
enum class IngredientSide { Included, Excluded }

/** A band of the recipe's own servings, never the scaled servings. [max] is `null` when the band has no top. */
enum class ServingsRange(val min: Int?, val max: Int?) {
    Any(min = null, max = null),
    OneToTwo(min = 1, max = 2),
    ThreeToFour(min = 3, max = 4),
    FiveToSix(min = 5, max = 6),
    SevenPlus(min = 7, max = null),
}
