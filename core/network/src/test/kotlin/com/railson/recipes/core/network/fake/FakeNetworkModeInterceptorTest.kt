package com.railson.recipes.core.network.fake

import com.google.common.truth.Truth.assertThat
import okhttp3.Call
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertThrows
import org.junit.Test
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class FakeNetworkModeInterceptorTest {

    private val config = FakeNetworkConfig()
    private val waits = mutableListOf<Duration>()
    private val client = OkHttpClient.Builder()
        .addInterceptor(FakeNetworkModeInterceptor(config, wait = { waits += it }))
        .addInterceptor(ServerAnswer())
        .build()
    private val request = Request.Builder().url("https://api.test/recipes").build()

    private fun call(): Response = client.newCall(request).execute()

    @Test
    fun `normal waits a little and lets the server answer`() {
        val response = call()

        assertThat(waits).containsExactly(600.milliseconds)
        assertThat(response.body.string()).isEqualTo("server")
    }

    @Test
    fun `slow waits 3 seconds and lets the server answer`() {
        config.mode = FakeNetworkMode.Slow

        val response = call()

        assertThat(waits).containsExactly(3.seconds)
        assertThat(response.body.string()).isEqualTo("server")
    }

    @Test
    fun `empty catalogue lets the server answer`() {
        config.mode = FakeNetworkMode.EmptyCatalogue

        val response = call()

        assertThat(response.body.string()).isEqualTo("server")
    }

    @Test
    fun `no network throws an unknown host`() {
        config.mode = FakeNetworkMode.NoNetwork

        assertThrows(UnknownHostException::class.java) { call() }
    }

    @Test
    fun `timeout throws a socket timeout`() {
        config.mode = FakeNetworkMode.Timeout

        assertThrows(SocketTimeoutException::class.java) { call() }
    }

    @Test
    fun `rate limited answers 429 with a retry after`() {
        config.mode = FakeNetworkMode.RateLimited

        val response = call()

        assertThat(response.code).isEqualTo(429)
        assertThat(response.header("Retry-After")).isEqualTo("10")
        assertThat(response.body.string()).contains("\"code\":\"RATE_LIMITED\"")
    }

    @Test
    fun `server error answers 500`() {
        config.mode = FakeNetworkMode.ServerError

        val response = call()

        assertThat(response.code).isEqualTo(500)
        assertThat(response.body.string()).contains("\"code\":\"SERVER_ERROR\"")
    }

    @Test
    fun `unavailable answers 503 with a retry after`() {
        config.mode = FakeNetworkMode.Unavailable

        val response = call()

        assertThat(response.code).isEqualTo(503)
        assertThat(response.header("Retry-After")).isEqualTo("30")
        assertThat(response.body.string()).contains("\"code\":\"UNAVAILABLE\"")
    }

    @Test
    fun `bad data answers 200 with a body that is not the server's`() {
        config.mode = FakeNetworkMode.BadData

        val response = call()

        assertThat(response.code).isEqualTo(200)
        assertThat(response.body.string()).isNotEqualTo("server")
    }

    @Test
    fun `a call canceled while waiting never reaches the server`() {
        val server = ServerAnswer()
        lateinit var call: Call
        val client = OkHttpClient.Builder()
            .addInterceptor(FakeNetworkModeInterceptor(config, wait = { call.cancel() }))
            .addInterceptor(server)
            .build()
        call = client.newCall(request)

        assertThrows(IOException::class.java) { call.execute() }
        assertThat(server.calls).isEqualTo(0)
    }
}

/** Stands in for the fake server: answers 200 "server" to any call that gets through, and counts them. */
private class ServerAnswer : Interceptor {
    var calls = 0

    override fun intercept(chain: Interceptor.Chain): Response = Response.Builder()
        .request(chain.request())
        .protocol(Protocol.HTTP_1_1)
        .code(200)
        .message("OK")
        .body("server".toResponseBody("text/plain".toMediaType()))
        .build()
        .also { calls++ }
}
