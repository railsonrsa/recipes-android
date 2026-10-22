package com.railson.recipes.recipes.data.mapper

import com.railson.recipes.recipes.domain.model.CallOutcome
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

/** Runs a call to the server and names how it ended. See docs/specs/data.md, "Call outcomes". */
internal suspend fun callServer(call: suspend () -> Unit): CallOutcome = try {
    call()
    CallOutcome.Success
} catch (@Suppress("SwallowedException") e: SocketTimeoutException) {
    // Each catch below turns the exception into its outcome, which is the whole report.
    CallOutcome.Timeout
} catch (@Suppress("SwallowedException") e: IOException) {
    CallOutcome.NoConnection
} catch (e: HttpException) {
    httpCallOutcome(e.code())
} catch (@Suppress("SwallowedException") e: SerializationException) {
    CallOutcome.BadData
}

@Suppress("MagicNumber") // HTTP status codes read better as numbers.
private fun httpCallOutcome(code: Int): CallOutcome = when (code) {
    400 -> CallOutcome.BadRequest
    404 -> CallOutcome.NotFound
    429 -> CallOutcome.RateLimited
    503 -> CallOutcome.Unavailable
    else -> CallOutcome.ServerError
}
