package com.railson.recipes.feature.recipedetail.detail

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class QuantityFormatTest {

    @Test
    fun `a whole quantity shows its unit after a space`() {
        assertThat(formatQuantity(200.0, "g")).isEqualTo("200 g")
    }

    @Test
    fun `no unit shows the quantity only`() {
        assertThat(formatQuantity(2.0, null)).isEqualTo("2")
    }

    @Test
    fun `quarters and halves show as fractions`() {
        assertThat(listOf(0.25, 0.5, 0.75).map { formatQuantity(it, null) }).containsExactly("¼", "½", "¾").inOrder()
    }

    @Test
    fun `a whole number and a half show together`() {
        assertThat(formatQuantity(1.5, "tbsp")).isEqualTo("1½ tbsp")
    }

    @Test
    fun `any other quantity rounds to one decimal place`() {
        assertThat(formatQuantity(4.0 / 3, "g")).isEqualTo("1.3 g")
    }

    @Test
    fun `a quantity that rounds to a whole number shows no decimal`() {
        assertThat(formatQuantity(1.98, "g")).isEqualTo("2 g")
    }

    @Test
    fun `one pinch is singular`() {
        assertThat(formatQuantity(1.0, "pinch")).isEqualTo("1 pinch")
    }

    @Test
    fun `more than one pinch is plural`() {
        assertThat(formatQuantity(2.0, "pinch")).isEqualTo("2 pinches")
    }

    @Test
    fun `less than one pinch is singular`() {
        assertThat(formatQuantity(0.5, "pinch")).isEqualTo("½ pinch")
    }

    @Test
    fun `more than one cup is plural`() {
        assertThat(formatQuantity(2.0, "cup")).isEqualTo("2 cups")
    }

    @Test
    fun `abbreviated units have no plural`() {
        assertThat(formatQuantity(2.0, "tbsp")).isEqualTo("2 tbsp")
    }

    @Test
    fun `a quantity shown as one stays singular`() {
        assertThat(formatQuantity(1.04, "pinch")).isEqualTo("1 pinch")
    }
}
