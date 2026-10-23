package com.railson.recipes.recipes.data.fake

import com.google.common.truth.Truth.assertThat
import com.railson.recipes.core.network.client.createJson
import com.railson.recipes.core.network.fake.FakeNetworkConfig
import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.Test
import java.io.File

class FakeRecipesInterceptorTest {

    private val client = OkHttpClient.Builder()
        .addInterceptor(
            FakeRecipesInterceptor(createJson(), FakeNetworkConfig()) {
                File("src/main/assets/recipes_page_1.json").readText()
            },
        )
        .build()

    private fun statusOf(query: String): Int =
        client.newCall(Request.Builder().url("https://api.recipes.fake/recipes?$query").build()).execute()
            .use { it.code }

    @Test
    fun `filters the app sends answer 200`() {
        val query = "page=1&pageSize=10&diet=vegan&diet=halal&minServings=3&maxServings=4&difficulty=1" +
            "&include=olive%20oil&include=basil&exclude=garlic&instructions=oven"

        assertThat(statusOf(query)).isEqualTo(200)
    }

    @Test
    fun `an unknown diet key answers 400`() {
        assertThat(statusOf("page=1&pageSize=10&diet=paleo")).isEqualTo(400)
    }

    @Test
    fun `a difficulty out of 1 to 3 answers 400`() {
        assertThat(statusOf("page=1&pageSize=10&difficulty=7")).isEqualTo(400)
    }

    @Test
    fun `min servings above max servings answers 400`() {
        assertThat(statusOf("page=1&pageSize=10&minServings=5&maxServings=2")).isEqualTo(400)
    }

    @Test
    fun `servings that are not a number answer 400`() {
        assertThat(statusOf("page=1&pageSize=10&minServings=many")).isEqualTo(400)
    }

    @Test
    fun `page 0 answers 400`() {
        assertThat(statusOf("page=0&pageSize=10")).isEqualTo(400)
    }
}
