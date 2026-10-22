package com.railson.recipes.recipes.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.railson.recipes.recipes.data.local.dao.SearchDao
import com.railson.recipes.recipes.data.local.entity.RecipeEntity
import com.railson.recipes.recipes.data.local.entity.SearchRemoteKeyEntity
import com.railson.recipes.recipes.data.local.entity.SearchResultEntity

@Database(
    entities = [RecipeEntity::class, SearchResultEntity::class, SearchRemoteKeyEntity::class],
    version = 1,
)
@TypeConverters(Converters::class)
internal abstract class RecipesDatabase : RoomDatabase() {
    abstract fun searchDao(): SearchDao
}
