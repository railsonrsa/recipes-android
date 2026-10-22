package com.railson.recipes.recipes.data.fake

import com.railson.recipes.recipes.data.remote.ErrorDto
import com.railson.recipes.recipes.data.remote.RecipePageDto
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

/**
 * The fake server: answers `GET /recipes` from the asset and never calls `chain.proceed()`.
 * See docs/specs/data.md, "Fake endpoint".
 */
internal class FakeRecipesInterceptor(
    private val json: Json,
    readAsset: () -> String,
) : Interceptor {

    private val catalog by lazy { json.decodeFromString<RecipePageDto>(readAsset()).data }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val params = request.searchParams()
        return when {
            request.method != "GET" || request.url.encodedPath != "/recipes" ->
                request.error(HTTP_NOT_FOUND, "NOT_FOUND", "No route for ${request.url.encodedPath}")
            params == null -> request.error(HTTP_BAD_REQUEST, "BAD_REQUEST", "Bad page or pageSize")
            else -> request.respond(HTTP_OK, json.encodeToString(FakeRecipeSearch.run(catalog, params)))
        }
    }

    private fun Request.searchParams(): FakeSearchParams? {
        val page = url.queryParameter("page")?.toIntOrNull()?.takeIf { it >= 1 }
        val pageSize = url.queryParameter("pageSize")?.toIntOrNull()?.takeIf { it in 1..MAX_PAGE_SIZE }
        return if (page != null && pageSize != null) FakeSearchParams(page, pageSize) else null
    }

    private fun Request.error(code: Int, errorCode: String, message: String): Response =
        respond(code, json.encodeToString(ErrorDto(errorCode, message)))

    private fun Request.respond(code: Int, body: String): Response = Response.Builder()
        .request(this)
        .protocol(Protocol.HTTP_1_1)
        .code(code)
        .message(if (code == HTTP_OK) "OK" else "Error")
        .body(body.toResponseBody("application/json".toMediaType()))
        .build()

    private companion object {
        const val HTTP_OK = 200
        const val HTTP_BAD_REQUEST = 400
        const val HTTP_NOT_FOUND = 404
        const val MAX_PAGE_SIZE = 50
    }
}
