package com.railson.recipes.recipes.domain.model

/**
 * The search text plus the filters (docs/specs/domain.md, Search). The empty search is the whole catalog.
 * The ingredient filters and the instruction search come with #25.
 */
data class SearchQuery(
    val text: String = "",
    val dietaryTags: Set<DietaryTag> = emptySet(),
    val servingsRange: ServingsRange = ServingsRange.Any,
    val difficulties: Set<Difficulty> = emptySet(),
) {
    /** The filter badge: one for each condition the reader turned on. The search text does not count. */
    val activeFilterCount: Int
        get() = dietaryTags.size + difficulties.size + if (servingsRange == ServingsRange.Any) 0 else 1

    companion object {
        val Catalog = SearchQuery()
    }
}

/** A band of the recipe's own servings, never the scaled servings. [max] is `null` when the band has no top. */
enum class ServingsRange(val min: Int?, val max: Int?) {
    Any(min = null, max = null),
    OneToTwo(min = 1, max = 2),
    ThreeToFour(min = 3, max = 4),
    FiveToSix(min = 5, max = 6),
    SevenPlus(min = 7, max = null),
}
