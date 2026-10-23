package com.railson.recipes.recipes.data.di

import android.content.Context
import androidx.room.Room
import com.railson.recipes.core.network.fake.FakeNetworkConfig
import com.railson.recipes.recipes.data.fake.FakeRecipesInterceptor
import com.railson.recipes.recipes.data.local.database.RecipesDatabase
import com.railson.recipes.recipes.data.remote.RecipesApi
import com.railson.recipes.recipes.data.repository.OfflineFirstRecipeRepository
import com.railson.recipes.recipes.domain.repository.RecipeRepository
import com.railson.recipes.recipes.domain.usecase.CountRecipesUseCase
import com.railson.recipes.recipes.domain.usecase.LoadNextPageUseCase
import com.railson.recipes.recipes.domain.usecase.ObserveRecipeUseCase
import com.railson.recipes.recipes.domain.usecase.ObserveSearchUseCase
import com.railson.recipes.recipes.domain.usecase.RefreshSearchUseCase
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module
import retrofit2.Retrofit

val recipesDataModule = module {
    single {
        Room.databaseBuilder(androidContext(), RecipesDatabase::class.java, "recipes.db").build()
    }
    single { get<RecipesDatabase>().recipeSearchDao() }
    single { get<RecipesDatabase>().recipeDao() }
    single {
        createRecipesApi(
            context = androidContext(),
            client = get(),
            retrofit = get(),
            json = get(),
            networkConfig = get(),
        )
    }
    single<RecipeRepository> { OfflineFirstRecipeRepository(api = get(), recipeSearchDao = get(), recipeDao = get()) }
    factoryOf(::ObserveSearchUseCase)
    factoryOf(::ObserveRecipeUseCase)
    factoryOf(::RefreshSearchUseCase)
    factoryOf(::LoadNextPageUseCase)
    factoryOf(::CountRecipesUseCase)
}

/** The recipes API, answered by the fake server after the shared client's interceptors. */
internal fun createRecipesApi(
    context: Context,
    client: OkHttpClient,
    retrofit: Retrofit,
    json: Json,
    networkConfig: FakeNetworkConfig,
): RecipesApi {
    val fakeServer = FakeRecipesInterceptor(json, networkConfig) {
        context.assets.open(CATALOG_ASSET).bufferedReader().use { it.readText() }
    }
    val recipesClient = client.newBuilder().addInterceptor(fakeServer).build()
    return retrofit.newBuilder().client(recipesClient).build().create(RecipesApi::class.java)
}

private const val CATALOG_ASSET = "recipes_page_1.json"
