package com.railson.recipes.recipes.domain.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class IngredientFilterTest {

    @Test
    fun `a word added as included is included`() {
        val query = SearchQuery.Catalog.withIngredient("garlic", IngredientSide.Included)

        assertThat(query.includedIngredients).containsExactly("garlic")
        assertThat(query.excludedIngredients).isEmpty()
    }

    @Test
    fun `a word added as excluded is excluded`() {
        val query = SearchQuery.Catalog.withIngredient("garlic", IngredientSide.Excluded)

        assertThat(query.excludedIngredients).containsExactly("garlic")
        assertThat(query.includedIngredients).isEmpty()
    }

    @Test
    fun `the word is trimmed`() {
        val query = SearchQuery.Catalog.withIngredient("  olive oil ", IngredientSide.Included)

        assertThat(query.includedIngredients).containsExactly("olive oil")
    }

    @Test
    fun `a blank word is ignored`() {
        val query = SearchQuery.Catalog.withIngredient("   ", IngredientSide.Included)

        assertThat(query).isEqualTo(SearchQuery.Catalog)
    }

    @Test
    fun `the same word in another case is not added twice`() {
        val query = SearchQuery(includedIngredients = setOf("garlic")).withIngredient("Garlic", IngredientSide.Included)

        assertThat(query.includedIngredients).containsExactly("garlic")
    }

    @Test
    fun `excluded word moves to included when added there`() {
        val query = SearchQuery(excludedIngredients = setOf("garlic")).withIngredient("garlic", IngredientSide.Included)

        assertThat(query.includedIngredients).containsExactly("garlic")
        assertThat(query.excludedIngredients).isEmpty()
    }

    @Test
    fun `included word moves to excluded when added there, whatever its case`() {
        val query = SearchQuery(includedIngredients = setOf("Garlic", "basil"))
            .withIngredient("garlic", IngredientSide.Excluded)

        assertThat(query.includedIngredients).containsExactly("basil")
        assertThat(query.excludedIngredients).containsExactly("garlic")
    }

    @Test
    fun `the side of a word is the side it is on`() {
        val query = SearchQuery(includedIngredients = setOf("basil"), excludedIngredients = setOf("garlic"))

        assertThat(query.sideOf("BASIL")).isEqualTo(IngredientSide.Included)
        assertThat(query.sideOf(" garlic")).isEqualTo(IngredientSide.Excluded)
        assertThat(query.sideOf("salt")).isNull()
    }

    @Test
    fun `removing a word takes it off its side`() {
        val query = SearchQuery(includedIngredients = setOf("basil"), excludedIngredients = setOf("garlic"))

        assertThat(query.withoutIngredient("garlic")).isEqualTo(SearchQuery(includedIngredients = setOf("basil")))
    }

    @Test
    fun `the same word with other accents is not added twice`() {
        val query = SearchQuery(includedIngredients = setOf("pão")).withIngredient("PAO", IngredientSide.Included)

        assertThat(query.includedIngredients).containsExactly("pão")
    }

    @Test
    fun `a word moves to the other side whatever its accents`() {
        val query = SearchQuery(includedIngredients = setOf("jalapeño"))
            .withIngredient("jalapeno", IngredientSide.Excluded)

        assertThat(query.includedIngredients).isEmpty()
        assertThat(query.excludedIngredients).containsExactly("jalapeno")
    }
}
