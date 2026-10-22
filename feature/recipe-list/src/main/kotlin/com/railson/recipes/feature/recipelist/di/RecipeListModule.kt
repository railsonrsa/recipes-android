package com.railson.recipes.feature.recipelist.di

import com.railson.recipes.feature.recipelist.list.RecipeListViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val recipeListModule = module {
    viewModelOf(::RecipeListViewModel)
}
