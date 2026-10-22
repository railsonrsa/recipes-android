package com.railson.recipes.core.network.di

import com.railson.recipes.core.network.BuildConfig
import com.railson.recipes.core.network.client.createJson
import com.railson.recipes.core.network.client.createOkHttpClient
import com.railson.recipes.core.network.client.createRetrofit
import com.railson.recipes.core.network.fake.FakeNetworkConfig
import com.railson.recipes.core.network.fake.FakeNetworkModeInterceptor
import org.koin.dsl.module

val networkModule = module {
    single { createJson() }
    single { FakeNetworkConfig() }
    single { createOkHttpClient(logBodies = BuildConfig.DEBUG, FakeNetworkModeInterceptor(config = get())) }
    single { createRetrofit(client = get(), json = get()) }
}
