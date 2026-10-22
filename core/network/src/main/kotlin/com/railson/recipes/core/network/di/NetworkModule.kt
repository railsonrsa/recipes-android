package com.railson.recipes.core.network.di

import com.railson.recipes.core.network.BuildConfig
import com.railson.recipes.core.network.client.createJson
import com.railson.recipes.core.network.client.createOkHttpClient
import com.railson.recipes.core.network.client.createRetrofit
import org.koin.dsl.module

val networkModule = module {
    single { createJson() }
    single { createOkHttpClient(logBodies = BuildConfig.DEBUG) }
    single { createRetrofit(client = get(), json = get()) }
}
