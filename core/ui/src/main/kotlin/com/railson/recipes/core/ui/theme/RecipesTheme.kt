package com.railson.recipes.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

@Composable
fun RecipesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalRecipeColors provides if (darkTheme) DarkRecipeColors else LightRecipeColors) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColors else LightColors,
            typography = RecipeTypography,
            shapes = RecipeShapes,
            content = content,
        )
    }
}

object RecipesTheme {
    /** The extended colors. M3 roles stay on `MaterialTheme.colorScheme`. */
    val colors: RecipeColors
        @Composable
        @ReadOnlyComposable
        get() = LocalRecipeColors.current
}
