package com.railson.recipes.recipes.data.local.database

import androidx.room.TypeConverter
import com.railson.recipes.recipes.data.remote.IngredientDto
import com.railson.recipes.recipes.data.remote.StepDto
import kotlinx.serialization.json.Json

/** Lists are stored as JSON text. Nothing queries inside them: search runs on the server. */
internal class Converters {
    @TypeConverter
    fun fromStrings(value: List<String>): String = Json.encodeToString(value)

    @TypeConverter
    fun toStrings(value: String): List<String> = Json.decodeFromString(value)

    @TypeConverter
    fun fromIngredients(value: List<IngredientDto>): String = Json.encodeToString(value)

    @TypeConverter
    fun toIngredients(value: String): List<IngredientDto> = Json.decodeFromString(value)

    @TypeConverter
    fun fromSteps(value: List<StepDto>): String = Json.encodeToString(value)

    @TypeConverter
    fun toSteps(value: String): List<StepDto> = Json.decodeFromString(value)
}
