package com.railson.recipes.recipes.data.repository

import com.google.common.truth.Truth.assertThat
import com.railson.recipes.recipes.domain.model.DietaryTag
import com.railson.recipes.recipes.domain.model.Difficulty
import com.railson.recipes.recipes.domain.model.SearchQuery
import com.railson.recipes.recipes.domain.model.ServingsRange
import org.junit.Test

class SearchKeyTest {

    @Test
    fun `the catalog has the empty key`() {
        assertThat(SearchQuery.Catalog.searchKey).isEqualTo("")
    }

    @Test
    fun `blank text is the catalog`() {
        assertThat(SearchQuery(text = "   ").searchKey).isEqualTo("")
    }

    @Test
    fun `text is trimmed, lower-cased and accent-stripped`() {
        assertThat(SearchQuery(text = "  Pão ").searchKey).isEqualTo("q=pao")
    }

    @Test
    fun `the same text in another case or with accents gives the same key`() {
        assertThat(SearchQuery(text = "PAO").searchKey).isEqualTo(SearchQuery(text = "pão").searchKey)
    }

    @Test
    fun `different text gives a different key`() {
        assertThat(SearchQuery(text = "lamb").searchKey).isNotEqualTo(SearchQuery(text = "lamb shoulder").searchKey)
    }

    @Test
    fun `dietary tags use the backend keys, sorted`() {
        val query = SearchQuery(dietaryTags = setOf(DietaryTag.Vegan, DietaryTag.GlutenFree))

        assertThat(query.searchKey).isEqualTo("diet=gluten-free&diet=vegan")
    }

    @Test
    fun `a servings range keeps both bounds`() {
        assertThat(SearchQuery(servingsRange = ServingsRange.ThreeToFour).searchKey)
            .isEqualTo("minServings=3&maxServings=4")
    }

    @Test
    fun `servings 7 or more has only the lower bound`() {
        assertThat(SearchQuery(servingsRange = ServingsRange.SevenPlus).searchKey).isEqualTo("minServings=7")
    }

    @Test
    fun `difficulties use the backend numbers, sorted`() {
        val query = SearchQuery(difficulties = setOf(Difficulty.Medium, Difficulty.Easy))

        assertThat(query.searchKey).isEqualTo("difficulty=1&difficulty=2")
    }

    @Test
    fun `text and filters keep a fixed order`() {
        val query = SearchQuery(
            text = "Pão",
            dietaryTags = setOf(DietaryTag.Vegetarian),
            servingsRange = ServingsRange.FiveToSix,
            difficulties = setOf(Difficulty.Medium),
        )

        assertThat(query.searchKey).isEqualTo("q=pao&diet=vegetarian&minServings=5&maxServings=6&difficulty=2")
    }

    @Test
    fun `any servings and no tags is still the catalog`() {
        assertThat(SearchQuery(servingsRange = ServingsRange.Any).searchKey).isEqualTo("")
    }

    @Test
    fun `ingredient words are normalized and sorted`() {
        val query = SearchQuery(
            includedIngredients = setOf("Olive oil", "basil"),
            excludedIngredients = setOf("Garlíc"),
        )

        assertThat(query.searchKey).isEqualTo("include=basil&include=olive oil&exclude=garlic")
    }

    @Test
    fun `ingredient words that normalize the same are sent once`() {
        val query = SearchQuery(includedIngredients = setOf("pão", "Pao"))

        assertThat(query.searchKey).isEqualTo("include=pao")
    }

    @Test
    fun `the instruction search is normalized`() {
        assertThat(SearchQuery(instructionSearch = " Sauté ").searchKey).isEqualTo("instructions=saute")
    }

    @Test
    fun `a blank instruction search is left out`() {
        assertThat(SearchQuery(instructionSearch = "  ").searchKey).isEqualTo("")
    }

    @Test
    fun `every part keeps the order of the endpoint's parameters`() {
        val query = SearchQuery(
            text = "pizza",
            difficulties = setOf(Difficulty.Medium),
            includedIngredients = setOf("basil"),
            excludedIngredients = setOf("garlic"),
            instructionSearch = "oven",
        )

        assertThat(query.searchKey).isEqualTo("q=pizza&difficulty=2&include=basil&exclude=garlic&instructions=oven")
    }
}
