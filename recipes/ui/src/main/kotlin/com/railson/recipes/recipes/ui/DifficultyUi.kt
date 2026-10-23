package com.railson.recipes.recipes.ui

import androidx.annotation.StringRes
import com.railson.recipes.recipes.domain.model.Difficulty

@get:StringRes
val Difficulty.label: Int
    get() = when (this) {
        Difficulty.Easy -> R.string.difficulty_easy
        Difficulty.Medium -> R.string.difficulty_medium
        Difficulty.Hard -> R.string.difficulty_hard
    }

@Suppress("MagicNumber") // Filled bars per difficulty, DESIGN.md 2.
val Difficulty.filledBars: Int
    get() = when (this) {
        Difficulty.Easy -> 1
        Difficulty.Medium -> 2
        Difficulty.Hard -> 3
    }
