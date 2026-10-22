package com.railson.recipes.core.network.fake

import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * "Is the network bad?": applies the current [FakeNetworkMode], knowing nothing about the API behind it.
 * See docs/specs/data.md, "Fake endpoint".
 */
class FakeNetworkModeInterceptor(
    private val config: FakeNetworkConfig,
    private val wait: (Duration) -> Unit = { Thread.sleep(it.inWholeMilliseconds) },
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val mode = config.mode
        // Every mode waits first, so the loading states are visible.
        wait(if (mode == FakeNetworkMode.Slow) SLOW_LATENCY else NORMAL_LATENCY)
        if (chain.call().isCanceled()) throw IOException("Canceled")
        return when (mode) {
            FakeNetworkMode.NoNetwork -> throw UnknownHostException("No network (fake mode)")
            FakeNetworkMode.Timeout -> throw SocketTimeoutException("Timeout (fake mode)")
            FakeNetworkMode.RateLimited -> request.error(HTTP_RATE_LIMITED, "RATE_LIMITED", "Too many requests")
                .header("Retry-After", RATE_LIMITED_RETRY_AFTER)
                .build()
            FakeNetworkMode.ServerError -> request.error(HTTP_SERVER_ERROR, "SERVER_ERROR", "Server error").build()
            FakeNetworkMode.Unavailable -> request.error(HTTP_UNAVAILABLE, "UNAVAILABLE", "Service unavailable")
                .header("Retry-After", UNAVAILABLE_RETRY_AFTER)
                .build()
            FakeNetworkMode.BadData -> request.answer(HTTP_OK, "OK", BROKEN_JSON).build()
            // Empty catalogue is the fake server's to answer: it knows what a catalog is.
            FakeNetworkMode.Normal, FakeNetworkMode.Slow, FakeNetworkMode.EmptyCatalogue -> chain.proceed(request)
        }
    }

    private fun Request.error(code: Int, errorCode: String, message: String): Response.Builder =
        answer(code, message, """{"code":"$errorCode","message":"$message"}""")

    private fun Request.answer(code: Int, message: String, body: String): Response.Builder = Response.Builder()
        .request(this)
        .protocol(Protocol.HTTP_1_1)
        .code(code)
        .message(message)
        .body(body.toResponseBody("application/json".toMediaType()))

    private companion object {
        val NORMAL_LATENCY = 600.milliseconds
        val SLOW_LATENCY = 3.seconds
        const val HTTP_OK = 200
        const val HTTP_RATE_LIMITED = 429
        const val HTTP_SERVER_ERROR = 500
        const val HTTP_UNAVAILABLE = 503
        const val RATE_LIMITED_RETRY_AFTER = "10"
        const val UNAVAILABLE_RETRY_AFTER = "30"
        const val BROKEN_JSON = """{"data": [{"id": """
    }
}
