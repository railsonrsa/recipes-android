package com.railson.recipes.recipes.domain.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ActiveFilterCountTest {

    @Test
    fun `the catalog has no active filters`() {
        assertThat(SearchQuery.Catalog.activeFilterCount).isEqualTo(0)
    }

    @Test
    fun `the search text does not count`() {
        assertThat(SearchQuery(text = "pao").activeFilterCount).isEqualTo(0)
    }

    @Test
    fun `each dietary tag counts once`() {
        val query = SearchQuery(dietaryTags = setOf(DietaryTag.Vegan, DietaryTag.GlutenFree))

        assertThat(query.activeFilterCount).isEqualTo(2)
    }

    @Test
    fun `each difficulty counts once`() {
        val query = SearchQuery(difficulties = setOf(Difficulty.Easy, Difficulty.Medium, Difficulty.Hard))

        assertThat(query.activeFilterCount).isEqualTo(3)
    }

    @Test
    fun `a servings range other than any counts once`() {
        assertThat(SearchQuery(servingsRange = ServingsRange.ThreeToFour).activeFilterCount).isEqualTo(1)
    }

    @Test
    fun `any servings does not count`() {
        assertThat(SearchQuery(servingsRange = ServingsRange.Any).activeFilterCount).isEqualTo(0)
    }

    @Test
    fun `vegetarian, servings 3 to 4 and easy count 3`() {
        val query = SearchQuery(
            text = "curry",
            dietaryTags = setOf(DietaryTag.Vegetarian),
            servingsRange = ServingsRange.ThreeToFour,
            difficulties = setOf(Difficulty.Easy),
        )

        assertThat(query.activeFilterCount).isEqualTo(3)
    }
}
