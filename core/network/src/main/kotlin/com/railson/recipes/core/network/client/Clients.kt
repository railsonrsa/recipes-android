package com.railson.recipes.core.network.client

import kotlinx.serialization.json.Json
import com.railson.recipes.core.network.fake.FakeNetworkModeInterceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

/** The fake server's host. No socket is ever opened to it: the fake interceptors answer first. */
private const val BASE_URL = "https://api.recipes.fake/"

fun createJson(): Json = Json { ignoreUnknownKeys = true }

/**
 * The logger comes first, so it sees what the fake interceptors answer. The API's fake server is added
 * after these with `newBuilder()`.
 */
fun createOkHttpClient(logBodies: Boolean, fakeNetworkMode: FakeNetworkModeInterceptor): OkHttpClient =
    OkHttpClient.Builder()
        .addInterceptor(
            HttpLoggingInterceptor().apply {
                level = if (logBodies) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
            },
        )
        .addInterceptor(fakeNetworkMode)
        .build()

fun createRetrofit(client: OkHttpClient, json: Json): Retrofit = Retrofit.Builder()
    .baseUrl(BASE_URL)
    .client(client)
    .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
    .build()
