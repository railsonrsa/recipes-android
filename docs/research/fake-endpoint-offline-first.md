# Fake HTTP endpoint and offline-first patterns

Research for issue #2. Checked on 2026-10-21.

Every claim cites the source that owns it. Lines marked **Recommendation** are our reading of those sources, not something a source says.

## Short answer

- Serve the bundled JSON from an **OkHttp application interceptor** that never calls `chain.proceed()`. It is added **after** `HttpLoggingInterceptor`. Retrofit, the converter, the logger and Retrofit's error mapping all run for real. No socket is opened.
- Do the filtering in a **plain Kotlin function**: (request URL query params) to (filtered, paged envelope). The interceptor just parses the URL and calls it. It can be unit-tested without OkHttp.
- Simulate latency with a sleep in the interceptor. Simulate HTTP errors by returning a `Response` with code 4xx/5xx. Simulate "no network" by **throwing an `IOException`** (never any other exception type). Control all three from one switch the debug UI can flip.
- Offline-first: the repository writes remote pages into Room, and the UI only observes Room `Flow`s. For search, cache **which recipes each query returned** (a query-results table plus a remote-keys table). Do not re-run the filters in SQL.
- Paging: with 10 recipes and one real page, a **manual approach** (Room `Flow` plus a `loadNextPage()` call) is smaller and easier to explain. Paging 3 `RemoteMediator` is the official pattern, but it is still `@ExperimentalPagingApi` and adds a lot of moving parts.

## Current stable versions (as of 2026-10-21)

Versions come from `maven-metadata.xml` on Maven Central and Google Maven. Dates come from the release notes.

| Library | Coordinate | Stable | Released | Notes |
|---|---|---|---|---|
| Retrofit | `com.squareup.retrofit2:retrofit` | 3.0.0 | 2025-05-15 | Still the latest. Its POM depends on OkHttp 4.12.0. |
| Retrofit kotlinx converter | `com.squareup.retrofit2:converter-kotlinx-serialization` | 3.0.0 | 2025-05-15 | First added in 2.10.0. |
| Retrofit mock | `com.squareup.retrofit2:retrofit-mock` | 3.0.0 | 2025-05-15 | See option C below. |
| OkHttp | `com.squareup.okhttp3:okhttp` | 5.5.0 | 2026-08-16 | Declaring it upgrades Retrofit's 4.12 dependency. Square says "We don't break binary compatibility with non-alpha APIs." |
| Logging interceptor | `com.squareup.okhttp3:logging-interceptor` | 5.5.0 | 2026-08-16 | |
| MockWebServer | `com.squareup.okhttp3:mockwebserver3` | 5.5.0 | 2026-08-16 | The old `okhttp3:mockwebserver` is marked "Obsolete. Depends on JUnit 4." |
| Room 2 | `androidx.room:room-*` | 2.8.5 | 2026-09-09 | |
| Room 3 | `androidx.room3:room3-*` | 3.0.3 | 2026-09-09 | New package and group. KSP only, needs an `SQLiteDriver`, coroutines first. |
| Paging | `androidx.paging:paging-runtime`, `paging-compose` | 3.5.3 | 2026-10-12 | `RemoteMediator` is still `@ExperimentalPagingApi`. |
| kotlinx.serialization | `org.jetbrains.kotlinx:kotlinx-serialization-json` | 1.12.0 | 2026-10-19 | Built on Kotlin 2.4.10. 1.11.0 (2026-04-09) is built on Kotlin 2.3.20. |
| Coroutines | `org.jetbrains.kotlinx:kotlinx-coroutines-core` | 1.11.0 | 2026-05-08 | |

Version trade-offs:

- **kotlinx.serialization 1.12.0 came out two days ago** and is built on Kotlin 2.4.10. If the project uses an older Kotlin or AGP, 1.11.0 is the safer pick. Match it to the Kotlin version the build uses.
- **Room 2.8.5 vs Room 3.0.3.** The Room 3 notes say it is "a major version update of Room 2.x package (`androidx.room`) that focuses on Kotlin Multiplatform." It also says "a `SQLiteDriver` is now *required*" and "DAO functions have to be suspending unless they are returning a reactive type". `PagingSource` support moved to `room3-paging` and must be registered with `@DaoReturnTypeConverters(PagingSourceDaoReturnTypeConverter::class)`. The Paging and offline-first guides still show `androidx.room` code. **Recommendation:** use Room 2.8.5 for an Android-only test, so the code matches the official guides. Room 3 is a fine choice if we want to show we're current, but it costs more setup.

Sources: [Retrofit CHANGELOG](https://github.com/square/retrofit/blob/trunk/CHANGELOG.md), [Retrofit 3.0.0 POM](https://repo1.maven.org/maven2/com/squareup/retrofit2/retrofit/3.0.0/retrofit-3.0.0.pom), [OkHttp CHANGELOG](https://github.com/square/okhttp/blob/main/CHANGELOG.md) (5.0.0 and 5.5.0 entries), [Room release notes](https://developer.android.com/jetpack/androidx/releases/room), [Room 3 release notes](https://developer.android.com/jetpack/androidx/releases/room3), [Paging release notes](https://developer.android.com/jetpack/androidx/releases/paging), [`RemoteMediator.kt`](https://github.com/androidx/androidx/blob/androidx-main/paging/paging-common/src/commonMain/kotlin/androidx/paging/RemoteMediator.kt), [kotlinx.serialization releases](https://github.com/Kotlin/kotlinx.serialization/releases), [kotlinx.coroutines 1.11.0](https://github.com/Kotlin/kotlinx.coroutines/releases/tag/1.11.0).

---

## Q1. Serving a bundled JSON as a real HTTP response

### Options

**A. OkHttp application interceptor that short-circuits (recommended)**

The OkHttp interceptor docs say application interceptors are "Permitted to short-circuit and not call `Chain.proceed()`" and "Are always invoked once". Application interceptors run in the order they are added. So the chain is:

```
Retrofit -> HttpLoggingInterceptor -> FakeRecipesInterceptor (returns a Response, no network)
```

The logger sees the real request and the fake response because it calls `proceed()` and the fake sits after it. The fake builds a `Response` with `request`, `protocol(HTTP_1_1)`, `code`, `message`, and a JSON body using `toResponseBody("application/json".toMediaType())`. Retrofit then runs the kotlinx converter, and `HttpException` is thrown for non-2xx codes (see Q3).

- Pros: no sockets, no threads to manage, no cleartext config, no test library in the app. It works the same on every API level. The base URL can be any fake host (for example `https://api.recipes.fake/`) because DNS is never used.
- Cons: network-level things never happen, such as real connect timeouts, TLS and socket drops. The interceptor has to fake them (see Q3). A `NetworkInterceptor` would **not** run, because there is no network step.
- Where it lives: a `:core:network` (or `:data:recipes-remote`) module, wired in through DI so a real base URL could replace it later.

Source: [OkHttp interceptors doc, "Choosing between application and network interceptors"](https://github.com/square/okhttp/blob/main/docs/features/interceptors.md). Logging setup and the warning about leaking headers and bodies are in the [logging-interceptor README](https://github.com/square/okhttp/blob/main/okhttp-logging-interceptor/README.md).

**B. MockWebServer running inside the app**

MockWebServer is "a scriptable web server for testing HTTP clients". Because "it exercises your full HTTP stack", you get real sockets, real timeouts and `throttleBody(...)` for slow responses. A `Dispatcher` can route by `request.url` instead of a queue, which is how you'd apply filters.

- Pros: the most realistic option. Real connect/read timeouts, real socket failures (`SocketEffect` in `mockwebserver3`), and real `HttpLoggingInterceptor` output including timing.
- Cons: it is a **test** library shipped in the app. It must start off the main thread and its lifecycle has to be managed. The URL is `http://127.0.0.1:<port>`, and "Starting with Android 9 (API level 28), cleartext support is disabled by default", so we need a `network-security-config` entry for localhost. Only Android 17 (API 37) and higher get an implicit localhost exception. It also needs the `INTERNET` permission.
- Best use: **JVM tests** of the Retrofit API. Its real home is `mockwebserver3` with the JUnit 5 `@StartStop` annotation.

Sources: [MockWebServer README](https://github.com/square/okhttp/blob/main/mockwebserver/README.md), [OkHttp 5.0.0 changelog](https://github.com/square/okhttp/blob/main/CHANGELOG.md) (new `mockwebserver3` coordinates, `SocketPolicy` replaced by `SocketEffect`), [Network security configuration](https://developer.android.com/privacy-and-security/security-config).

**C. `retrofit-mock` (`MockRetrofit` + `BehaviorDelegate` + `NetworkBehavior`)**

You write the service implementation yourself and return through `BehaviorDelegate.returningResponse(...)`, which wraps `Calls.response(...)`. OkHttp is never called, so **`HttpLoggingInterceptor` and the converter do not run**. That fails the brief. Its `NetworkBehavior` is still a useful model for knobs: defaults are a "2 second delay with 40% variance. Failures will occur 3% of the time. HTTP errors will occur 0% of the time."

Sources: [`NetworkBehavior.java`](https://github.com/square/retrofit/blob/trunk/retrofit-mock/src/main/java/retrofit2/mock/NetworkBehavior.java), [`BehaviorDelegate.java`](https://github.com/square/retrofit/blob/trunk/retrofit-mock/src/main/java/retrofit2/mock/BehaviorDelegate.java).

**D. A real local server (json-server, Ktor on the host, etc.)**

This needs a process outside the app, plus `10.0.2.2` on the emulator. A reviewer cannot just run the APK. Rejected.

### Recommendation

Use A in the app. Use the same interceptor, or MockWebServer, in JVM tests of the API. Keep the asset in `src/main/assets/` of the fake module, or in a debug source set if we want release builds to point at a real URL.

---

## Q2. Applying query parameters inside the fake endpoint

### How the parameters travel

Retrofit's `@Query` docs say: "`null` values are ignored. Passing a `List` or array will result in a query parameter for each non-`null` item". Calling with `("coworker", "bowling")` "yields `/friends?group=coworker&group=bowling`". So the API can be:

```kotlin
@GET("recipes/search")
suspend fun search(
    @Query("q") text: String?,
    @Query("diet") diets: List<String>?,          // must have ALL
    @Query("minServings") minServings: Int?,
    @Query("maxServings") maxServings: Int?,
    @Query("difficulty") difficulties: List<Int>?, // ANY of 1/2/3
    @Query("include") include: List<String>?,     // must have ALL
    @Query("exclude") exclude: List<String>?,     // must have NONE
    @Query("instructions") instructionText: String?,
    @Query("page") page: Int,
    @Query("pageSize") pageSize: Int,
): RecipePageDto
```

On the fake side, OkHttp's `HttpUrl` reads them back with `queryParameter(name)` for one value and `queryParameterValues(name)` for repeated values.

Source: [`Query.java` Javadoc](https://github.com/square/retrofit/blob/trunk/retrofit/src/main/java/retrofit2/http/Query.java).

### Shape (recommendation)

1. `FakeRecipesInterceptor` matches only `GET /recipes/search` and returns 404 for anything else, like a real server.
2. It parses the URL into a `SearchParams` value. On bad input (for example `page=0` or `difficulty=7`) it returns **400**, so the error path is real.
3. A pure function `FakeRecipeSearch.run(all: List<RecipeDto>, params): RecipePageDto` does the work:
   - text: case-insensitive match on title and description (decide the exact fields in the spec)
   - diets: recipe tags contain **all** requested diets
   - servings: `min <= servings <= max`, both optional
   - difficulty: recipe difficulty is **in** the requested set
   - include: every requested ingredient matches some recipe ingredient
   - exclude: no requested ingredient matches any recipe ingredient
   - instructions: any step contains the text
   - then paging: `drop((page - 1) * pageSize).take(pageSize)`, plus `pagination` built from the **filtered** count (`totalItems`, `totalPages`, `hasNext`, `nextPage`, `previousPage`)
4. The asset only has page 1. If `page` goes past the filtered data, return `data: []` with `hasNext = false`. This covers "page 2 must return `[]`".

The asset is parsed once and kept in memory, with kotlinx.serialization and `ignoreUnknownKeys = true`. The response body is re-encoded from the filtered DTOs, so the HTTP body really changes per query. The logger shows that.

Decisions still open (for the spec, not research): how ingredient matching works (exact name vs contains, case and accent folding) and which fields free text searches.

---

## Q3. Simulating latency, HTTP errors and "no network"

What the sources say:

- With `suspend` functions, Retrofit's `await()` resumes with `HttpException(response)` when `!response.isSuccessful`. It passes `onFailure` throwables (network `IOException`s) straight through. If the return type is `Response<T>`, no exception is thrown for HTTP errors. ([`KotlinExtensions.kt`](https://github.com/square/retrofit/blob/trunk/retrofit/src/main/java/retrofit2/KotlinExtensions.kt))
- In an async OkHttp call, if an interceptor throws something that is **not** an `IOException`, OkHttp cancels the call, reports `IOException("canceled due to ...")` to the callback, and then **rethrows the original on the dispatcher thread**, which crashes the app. So the fake must only throw `IOException` subclasses. ([`RealCall.kt` `AsyncCall.run`](https://github.com/square/okhttp/blob/main/okhttp/src/commonJvmAndroid/kotlin/okhttp3/internal/connection/RealCall.kt))
- Application interceptors "Can adjust Call timeouts" and may call `proceed()` several times. ([interceptors doc](https://github.com/square/okhttp/blob/main/docs/features/interceptors.md))

Recommended knobs in a `FakeNetworkConfig`. Hold it in memory and expose it to a debug menu or a dev "Network" toggle in settings.

| Mode | How the interceptor does it | What the app sees |
|---|---|---|
| Latency | `Thread.sleep(delay ± jitter)` before returning. This runs on OkHttp's dispatcher thread, not the main thread. Check `chain.call().isCanceled()` after sleeping and throw `IOException("Canceled")` if it is. | Slow load. Loading state is visible. |
| HTTP error | Return `Response` with code 500, 503 or 429 and a small JSON error body. | `HttpException` (code available) |
| No network | `throw UnknownHostException("Fake: no network")` | `IOException`, so we show cached data plus an "offline" message |
| Timeout | `throw SocketTimeoutException("Fake: timeout")` | `IOException` |
| Malformed body (optional) | Return 200 with broken JSON | `SerializationException` from the converter. The repository must catch it. |

Trade-offs:

- With option A, the timeouts are **faked**: OkHttp's real call/read timeouts do not fire, because there's no socket. Only MockWebServer (option B) gives real timeouts.
- Random failures like `NetworkBehavior`'s 3% make a demo flaky. **Recommendation:** use deterministic modes (Off / Slow / Error 500 / No network) picked by hand. Allow an optional fixed `Random(seed)` in tests.
- "No network" in the fake is separate from the real device's connectivity. If the spec also wants a real offline banner, that needs `ConnectivityManager`, which is a different ticket.

---

## Q4. Offline-first repository with Room as the single source of truth

### What the official guide says

- "The local data source is the canonical source of truth for the app. It should be the exclusive source of any data that higher layers of the app read."
- "The domain and UI layers of the app must never communicate directly with the network layer. It is the responsibility of the hosting `repository` to communicate with it, and use it to update the local data source."
- "In an offline-first app, read operations from repositories read directly from the local data source."
- Pull-based sync "works best when the app expects brief to intermediate periods of no network connectivity", and "This is the pattern used by the Jetpack Paging Library with its `RemoteMediator` API."
- On errors in local-read `Flow`s: `catch` stops the crash but "the backing `Flow` still terminates". Use `retry`, and model UI state as Loading/Content/Error.
- On network reads: retry only connectivity errors, with exponential backoff.

Source: [Build an offline-first app](https://developer.android.com/topic/architecture/data-layer/offline-first).

### Paging 3 `RemoteMediator` (official pattern)

From [Page from network and database](https://developer.android.com/topic/libraries/architecture/paging/v3-network-db):

- "`RemoteMediator` acts as a signal from the Paging library when the app has run out of cached data." `load()` fetches and writes to Room. The Room `PagingSource` invalidates and reloads.
- "the app uses the database as the source of truth … an in-memory cache in the `ViewModel` is unnecessary."
- `initialize()` returns `LAUNCH_INITIAL_REFRESH` or `SKIP_INITIAL_REFRESH`, for example based on a cache timeout.
- `load(LoadType, PagingState)` handles REFRESH (start over, clear old rows for this query), PREPEND (usually `endOfPaginationReached = true`) and APPEND (next key).
- "When remote keys are not directly associated with list items, it is best to store them in a separate table". The guide's `RemoteKey(label = query, nextKey)` is keyed by **query**.
- Writes go in one transaction: "Store loaded data, and next key in transaction, so that they're always consistent."
- The `Pager` takes `pagingSourceFactory = { dao.pagingSource(query) }`, not a fixed `PagingSource`.
- `RemoteMediator` is annotated `@ExperimentalPagingApi` in the current source.

Pros: the official answer. It handles load states (`LoadState.mediator`), retry, and footer loading in `LazyColumn` through `paging-compose`.
Cons: it's experimental, the moving parts are many (mediator, remote keys, transaction, invalidation), and it's hard to test well. It's overkill for 10 recipes and one page.

### Manual approach (recommended for this scope)

Same single source of truth, fewer parts:

```kotlin
interface RecipeRepository {
    fun observeSearch(query: SearchQuery): Flow<SearchResult>   // reads Room only
    suspend fun refresh(query: SearchQuery): Result<Unit>        // page 1, replaces rows for this query
    suspend fun loadNextPage(query: SearchQuery): Result<Unit>   // uses stored nextPage
}
```

- `observeSearch` combines the Room `Flow` of results for that query with the stored remote key. That gives `hasNext` and `lastUpdated`.
- `refresh` and `loadNextPage` call the API, then write everything in one Room transaction: `withTransaction { … }` in Room 2, `withWriteTransaction` in Room 3. On failure they return the error. The ViewModel shows it as a snackbar or banner **over** whatever Room already shows.
- The ViewModel turns this into the screen's UI state through `stateIn(viewModelScope, WhileSubscribed(5_000), …)`, as the offline-first guide shows.

Pros: easy to read, easy to test with a fake API and an in-memory Room, and no experimental API. Cons: we write the "load more when near the end" trigger in Compose ourselves, and we don't get Paging's load-state plumbing.

### How to cache search results

Problem: different filter combinations return different subsets. "Which recipes match" belongs to the **server**.

Recommended tables:

- `recipes(id PK, …)`: one row per recipe, upserted. Detail screens read from here. It is shared across queries, so no duplicates.
- `search_results(queryKey, position, recipeId)` with PK `(queryKey, position)`: ordered results per query.
- `search_remote_keys(queryKey PK, nextPage, totalItems, lastUpdated)`: paging and freshness per query. This is the guide's "separate table" for keys.

`queryKey` is a **canonical** string of the normalized query: trimmed and lower-cased text, sorted lists, fixed field order, no `page` and no `pageSize`. That way the same filters in any order hit the same cache entry.

On REFRESH: delete `search_results` and the remote key for that `queryKey`, then insert the new ones, all in one transaction. Never delete from `recipes` here, because other queries may point at them.

Offline behavior with this design:

- A query seen before shows its cached results.
- A **new** query while offline has no cache entry. Show "You're offline. Results for these filters aren't saved yet." Optional fallback: show the default "all recipes" list.

Alternative: **re-run the filters locally in SQL** (Room `LIKE` or FTS and joins on tags and ingredients). It works offline for any filter combination, but it duplicates the server's filter logic in a second language. The two can drift apart, and the remote stops being "the" source of results. **Recommendation:** don't, or keep it as a "would do with more time" item.

Cache freshness: use a `lastUpdated` timestamp per `queryKey`. This is the same idea as the guide's `initialize()` cache timeout. Show cached data right away and refresh in the background when it is stale.

---

## Things the spec still has to decide (from this research)

1. Option A (interceptor) as the app's fake. Confirm.
2. Exact matching rules for text, ingredients and diets (case, accents, contains vs equals).
3. Manual paging vs `RemoteMediator`.
4. Room 2.8.5 vs Room 3.0.3; kotlinx.serialization 1.11.0 vs 1.12.0 (depends on the Kotlin version).
5. Fake network modes and where the toggle lives (debug menu, settings, or build flag).
6. What a never-seen query shows while offline.
