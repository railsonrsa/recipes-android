package com.railson.recipes.recipes.data.fake

import com.railson.recipes.core.network.fake.FakeNetworkConfig
import com.railson.recipes.core.network.fake.FakeNetworkMode
import com.railson.recipes.recipes.data.remote.ErrorDto
import com.railson.recipes.recipes.data.remote.RecipePageDto
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import java.io.IOException

/**
 * The fake server: answers `GET /recipes` from the asset and never calls `chain.proceed()`.
 * See docs/specs/data.md, "Fake endpoint".
 */
internal class FakeRecipesInterceptor(
    private val json: Json,
    private val networkConfig: FakeNetworkConfig,
    readAsset: () -> String,
) : Interceptor {

    private val catalog by lazy { json.decodeFromString<RecipePageDto>(readAsset()).data }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val catalog = try {
            if (networkConfig.mode == FakeNetworkMode.EmptyCatalogue) emptyList() else catalog
        } catch (e: IllegalArgumentException) {
            // Only IOExceptions may leave an interceptor. Anything else crashes OkHttp's dispatcher.
            throw IOException("The fake catalog asset doesn't parse", e)
        }
        val params = request.searchParams()
        return when {
            request.method != "GET" || request.url.encodedPath != "/recipes" ->
                request.error(HTTP_NOT_FOUND, "NOT_FOUND", "No route for ${request.url.encodedPath}")
            params == null -> request.error(HTTP_BAD_REQUEST, "BAD_REQUEST", "Bad parameter")
            else -> request.respond(HTTP_OK, json.encodeToString(FakeRecipeSearch.run(catalog, params)))
        }
    }

    /** `null` for a bad parameter (docs/specs/data.md, Errors). */
    private fun Request.searchParams(): FakeSearchParams? {
        val page = url.queryParameter("page")?.toIntOrNull()?.takeIf { it >= 1 }
        val pageSize = url.queryParameter("pageSize")?.toIntOrNull()?.takeIf { it in 1..MAX_PAGE_SIZE }
        val diet = url.queryParameterValues("diet").filterNotNull().toSet()
        val difficulties = url.queryParameterValues("difficulty").map { it?.toIntOrNull() }.toSet()
        val minServingsText = url.queryParameter("minServings")
        val maxServingsText = url.queryParameter("maxServings")
        val minServings = minServingsText?.toIntOrNull()
        val maxServings = maxServingsText?.toIntOrNull()
        val valid = page != null && pageSize != null &&
            diet.all { it in DIET_KEYS } &&
            difficulties.all { it in DIFFICULTIES } &&
            (minServingsText == null) == (minServings == null) &&
            (maxServingsText == null) == (maxServings == null) &&
            (minServings == null || maxServings == null || minServings <= maxServings)
        return if (!valid) null else FakeSearchParams(
            page = page,
            pageSize = pageSize,
            text = url.queryParameter("q"),
            diet = diet,
            minServings = minServings,
            maxServings = maxServings,
            difficulties = difficulties.filterNotNull().toSet(),
        )
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
        // The fake server keeps its own list of keys, like a real backend, instead of reading the app's mapper.
        val DIFFICULTIES = 1..3
        val DIET_KEYS = setOf(
            "vegan", "vegetarian", "pescatarian", "halal", "gluten-free", "dairy-free", "nut-free", "low-carb",
        )
    }
}
