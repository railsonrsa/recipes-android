package com.railson.recipes

import android.app.Application
import com.railson.recipes.core.network.di.networkModule
import com.railson.recipes.feature.recipedetail.di.recipeDetailModule
import com.railson.recipes.feature.recipelist.di.recipeListModule
import com.railson.recipes.recipes.data.di.recipesDataModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class RecipesApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@RecipesApplication)
            modules(networkModule, recipesDataModule, recipeListModule, recipeDetailModule)
        }
    }
}
