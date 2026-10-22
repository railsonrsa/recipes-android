package com.railson.recipes.feature.recipedetail.di

import com.railson.recipes.feature.recipedetail.detail.RecipeDetailViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val recipeDetailModule = module {
    viewModel { (id: String) -> RecipeDetailViewModel(id = id, observeRecipe = get()) }
}
