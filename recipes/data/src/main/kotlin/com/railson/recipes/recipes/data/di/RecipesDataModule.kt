package com.railson.recipes.recipes.data.di

import androidx.room.Room
import com.railson.recipes.recipes.data.fake.FakeRecipesInterceptor
import com.railson.recipes.recipes.data.local.database.RecipesDatabase
import com.railson.recipes.recipes.data.remote.RecipesApi
import com.railson.recipes.recipes.data.repository.OfflineFirstRecipeRepository
import com.railson.recipes.recipes.domain.repository.RecipeRepository
import com.railson.recipes.recipes.domain.usecase.ObserveSearchUseCase
import com.railson.recipes.recipes.domain.usecase.RefreshSearchUseCase
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module
import retrofit2.Retrofit

val recipesDataModule = module {
    single {
        Room.databaseBuilder(androidContext(), RecipesDatabase::class.java, "recipes.db").build()
    }
    single { get<RecipesDatabase>().searchDao() }
    single<RecipesApi> {
        val context = androidContext()
        val fakeServer = FakeRecipesInterceptor(json = get()) {
            context.assets.open(ASSET).bufferedReader().use { it.readText() }
        }
        val client = get<OkHttpClient>().newBuilder().addInterceptor(fakeServer).build()
        get<Retrofit>().newBuilder().client(client).build().create(RecipesApi::class.java)
    }
    single<RecipeRepository> { OfflineFirstRecipeRepository(api = get(), dao = get()) }
    factoryOf(::ObserveSearchUseCase)
    factoryOf(::RefreshSearchUseCase)
}

internal const val ASSET = "recipes_page_1.json"
