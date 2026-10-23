package com.railson.recipes.recipes.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.railson.recipes.recipes.data.local.entity.RecipeEntity
import kotlinx.coroutines.flow.Flow

@Dao
internal interface RecipeDao {

    @Query("SELECT * FROM recipes WHERE id = :id")
    fun observeRecipe(id: String): Flow<RecipeEntity?>
}
