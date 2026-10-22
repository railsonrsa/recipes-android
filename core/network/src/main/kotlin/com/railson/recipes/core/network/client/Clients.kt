package com.railson.recipes.core.network.client

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

/** The fake server's host. No socket is ever opened to it: the fake interceptors answer first. */
private const val BASE_URL = "https://api.recipes.fake/"

fun createJson(): Json = Json { ignoreUnknownKeys = true }

/** Fake interceptors are added after this one with `newBuilder()`, so the logger sees what they answer. */
fun createOkHttpClient(logBodies: Boolean): OkHttpClient = OkHttpClient.Builder()
    .addInterceptor(
        HttpLoggingInterceptor().apply {
            level = if (logBodies) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        },
    )
    .build()

fun createRetrofit(client: OkHttpClient, json: Json): Retrofit = Retrofit.Builder()
    .baseUrl(BASE_URL)
    .client(client)
    .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
    .build()
