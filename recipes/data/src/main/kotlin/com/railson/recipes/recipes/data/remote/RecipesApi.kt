package com.railson.recipes.recipes.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

internal interface RecipesApi {
    // Retrofit leaves a null parameter out of the URL and sends a list once per value.
    @Suppress("LongParameterList") // One parameter per query parameter in docs/specs/data.md.
    @GET("recipes")
    suspend fun getRecipes(
        @Query("q") text: String?,
        @Query("diet") diet: List<String>,
        @Query("minServings") minServings: Int?,
        @Query("maxServings") maxServings: Int?,
        @Query("difficulty") difficulties: List<Int>,
        @Query("include") include: List<String>,
        @Query("exclude") exclude: List<String>,
        @Query("instructions") instructions: String?,
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int,
    ): RecipePageDto
}
