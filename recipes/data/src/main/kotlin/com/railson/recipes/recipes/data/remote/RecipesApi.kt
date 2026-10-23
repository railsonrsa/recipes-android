package com.railson.recipes.recipes.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

internal interface RecipesApi {
    @GET("recipes")
    suspend fun getRecipes(
        // Retrofit leaves a null parameter out of the URL.
        @Query("q") text: String?,
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int,
    ): RecipePageDto
}
