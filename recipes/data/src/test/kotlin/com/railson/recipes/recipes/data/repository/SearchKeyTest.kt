package com.railson.recipes.recipes.data.repository

import com.google.common.truth.Truth.assertThat
import com.railson.recipes.recipes.domain.model.SearchQuery
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
}
