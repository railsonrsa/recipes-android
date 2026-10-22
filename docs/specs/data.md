# Data spec

How the app gets recipes: the fake endpoint's contract, the offline-first cache, and how loading, paging, refresh and errors behave. Terms are in [GLOSSARY.md](../../GLOSSARY.md), domain rules in [domain.md](domain.md). Decided in issue #4. The fake endpoint split into two interceptors comes from issue #5. Research: [fake-endpoint-offline-first.md](../research/fake-endpoint-offline-first.md).

## The picture

```
UI ← ViewModel ← Room (Flow)          the UI only reads Room
                   ↑ writes
              repository → Retrofit → HttpLoggingInterceptor → FakeRecipesInterceptor → asset JSON
```

Room is the only source the UI reads. The repository calls the network and writes what comes back into Room.

## Endpoint

One route: `GET https://api.recipes.fake/recipes`. The host is fake. No socket is opened.

With no filters it returns the whole catalog (listing). With filters it acts as a search. In both cases the fake does the same three steps: **filter**, then **page** what is left, then build `pagination` from the **filtered** count.

### Query parameters

All are optional except `page` and `pageSize`. Repeated parameters are sent once per value (`diet=vegan&diet=halal`).

| Parameter | Type | Meaning |
|---|---|---|
| `q` | String | Search text |
| `diet` | repeated String | Dietary tag keys (`vegan`, `gluten-free`, ...). Must have **all** |
| `minServings` | Int | Servings range, lower bound |
| `maxServings` | Int | Servings range, upper bound |
| `difficulty` | repeated Int | `1`, `2`, `3`. Must be **any** |
| `include` | repeated String | Included ingredients |
| `exclude` | repeated String | Excluded ingredients |
| `instructions` | String | Instruction search |
| `page` | Int | Starts at 1 |
| `pageSize` | Int | 1 to 50. The app uses 10 |

Servings range to parameters: Any = none, 1–2 = `minServings=1&maxServings=2`, 3–4, 5–6 the same way, 7+ = `minServings=7` only.

Matching rules are the ones in [domain.md](domain.md#search).

### Success: 200

```json
{
  "data": [ { "id": "…", "title": "…", "prepTimeSeconds": 900, "cookTimeSeconds": 1200, "…": "…" } ],
  "pagination": {
    "page": 1, "pageSize": 10, "totalItems": 10, "totalPages": 1,
    "hasNext": false, "nextPage": null, "previousPage": null
  }
}
```

- The asset renames `prepTimeMinutes` → `prepTimeSeconds` and `cookTimeMinutes` → `cookTimeSeconds` (values × 60). See [domain.md](domain.md#time).
- A page past the end returns `data: []` and `hasNext: false`.

Example with 15 recipes on the server and `pageSize=10`:

| Request | `data` | `pagination` |
|---|---|---|
| `/recipes?page=1` | r1…r10 | total 15, `hasNext: true`, `nextPage: 2` |
| `/recipes?page=2` | r11…r15 | total 15, `hasNext: false` |
| `/recipes?q=lamb&page=1` | r12, r14 | total 2, `hasNext: false` |
| `/recipes?q=lamb&page=2` | `[]` | total 2, `hasNext: false` |

### Errors

Error responses have a small body. The app never shows `message` on screen. It is for logs.

```json
{ "code": "RATE_LIMITED", "message": "Too many requests" }
```

| Status | `code` | When |
|---|---|---|
| 400 | `BAD_REQUEST` | Bad parameter: `page=0`, `pageSize=99`, `difficulty=7`, unknown diet key, `minServings > maxServings` |
| 404 | `NOT_FOUND` | Any route other than `GET /recipes`. The app never calls one |
| 429 | `RATE_LIMITED` | Fake mode. Header `Retry-After: 10` |
| 500 | `SERVER_ERROR` | Fake mode |
| 503 | `UNAVAILABLE` | Fake mode. Header `Retry-After: 30` |

Unknown query parameters are ignored.

## Call outcomes

Every call ends in one named outcome. The repository maps exceptions to it. Tests use these names.

| Outcome | Cause | UI treats it as |
|---|---|---|
| `Success` | 2xx with a valid body | — |
| `NoConnection` | `UnknownHostException`, `ConnectException` | Offline |
| `Timeout` | `SocketTimeoutException` | Offline |
| `BadRequest` | 400 | Generic |
| `NotFound` | 404 | Generic |
| `RateLimited` | 429 | Generic |
| `ServerError` | 500 | Generic |
| `Unavailable` | 503 | Generic |
| `BadData` | 200 with a body that doesn't parse (`SerializationException`) | Generic |

The UI only tells **Offline** from **Generic**. `Retry-After` is logged, never acted on.

## Fake endpoint

Two OkHttp application interceptors, added **after** `HttpLoggingInterceptor`, in this order:

```
HttpLoggingInterceptor → FakeNetworkModeInterceptor → FakeRecipesInterceptor → asset JSON
                         (:core:network)               (:recipes:data)
                         "is the network bad?"         "what is the answer?"
```

- `FakeNetworkModeInterceptor` knows nothing about recipes. It applies the current fake network mode: it waits, throws, or answers an error. In **Normal**, **Slow** and **Empty catalogue** it waits and passes the call on.
- `FakeRecipesInterceptor` never calls `chain.proceed()`. So Retrofit, the converter and the logger all run for real.
- It parses the URL and calls a pure function, `FakeRecipeSearch.run(recipes, params)`, which can be tested without OkHttp. The asset is parsed once and kept in memory.
- Both only throw `IOException` subclasses. Any other exception crashes OkHttp's dispatcher.
- `HttpLoggingInterceptor` logs `BODY` in debug builds and `NONE` in release builds.

### Fake network modes

`FakeNetworkConfig` (in `:core:network`) lives in memory and goes back to **Normal** when the app restarts. A ⋮ "Network" menu on the list screen switches it. The menu exists only in debug builds. See [architecture.md](architecture.md#debug-network-menu).

| Mode | Handled by | What happens | Outcome |
|---|---|---|---|
| Normal | both | Waits ~600 ms, answers 200 | `Success` |
| Slow | both | Waits 3 s, answers 200 | `Success` |
| Empty catalogue | `FakeRecipesInterceptor` | Answers 200 with `data: []`, total 0 | `Success` |
| No network | `FakeNetworkModeInterceptor` | Throws `UnknownHostException` | `NoConnection` |
| Timeout | `FakeNetworkModeInterceptor` | Throws `SocketTimeoutException` | `Timeout` |
| Rate limited | `FakeNetworkModeInterceptor` | Answers 429 | `RateLimited` |
| Server error | `FakeNetworkModeInterceptor` | Answers 500 | `ServerError` |
| Unavailable | `FakeNetworkModeInterceptor` | Answers 503 | `Unavailable` |
| Bad data | `FakeNetworkModeInterceptor` | Answers 200 with broken JSON | `BadData` |

`FakeNetworkModeInterceptor` waits the Normal latency first in every mode, so loading states are visible. After waiting, it throws `IOException("Canceled")` if the call was canceled.

## Cache (Room)

The cache keeps **what the server returned for each search**. The app never re-runs the filters locally. See [ADR 0001](../adr/0001-cache-server-results-per-search.md).

| Table | Columns | Purpose |
|---|---|---|
| `recipes` | `id` PK, all recipe fields | One row per recipe, upserted. Shared by every search. The detail screen reads from here |
| `search_results` | `searchKey`, `position`, `recipeId`. PK (`searchKey`, `position`) | Which recipes a search returned, in order |
| `search_remote_keys` | `searchKey` PK, `nextPage`, `totalItems`, `lastUpdated` | Paging and freshness per search |

`searchKey` is a canonical string of the search: text and words trimmed, lower-cased and accent-stripped, lists sorted, fixed field order, no `page` or `pageSize`. The same search in any order gives the same key. The catalog is the empty search.

Writes:

- **Refresh** (page 1): in one transaction, upsert the recipes, delete the `search_results` rows and the remote key for that `searchKey`, then insert the new ones.
- **Next page**: in one transaction, upsert the recipes, append rows after the last `position`, update the remote key.
- Rows in `recipes` are never deleted. Other searches may point at them.

Example, 15 recipes on the server, `pageSize = 10`:

| Step | Request | `recipes` | `search_results` |
|---|---|---|---|
| 1 | Catalog, page 1 → r1…r10 | r1…r10 | `""` → r1…r10, next page 2, total 15 |
| 2 | "lamb" → r12, r14 | + r12, r14 | `q=lamb` → r12, r14 |
| 3 | "lamb shoulder" → r14 | r14 updated | `q=lamb shoulder` → r14 |

At step 3, r14 is already saved, but the search "lamb shoulder" is new. Online, the screen loads and then fills. Offline, the app doesn't know which recipes match, so it shows the offline message. The detail of r14 opens offline either way.

## Repository

```kotlin
interface RecipeRepository {
    fun observeSearch(query: SearchQuery): Flow<SearchResult>     // reads Room only
    fun observeRecipe(id: String): Flow<Recipe?>              // reads Room only
    suspend fun refresh(query: SearchQuery): CallOutcome          // page 1, always hits the network
    suspend fun loadNextPage(query: SearchQuery): CallOutcome     // uses the stored nextPage
    suspend fun count(query: SearchQuery): Int?                   // null when the call fails
}
```

- Paging is manual. No Paging 3 `RemoteMediator`.
- `SearchResult.hasMore` comes from the stored remote key.
- `count` calls `page=1&pageSize=1`, reads `totalItems` and writes nothing to Room.

## Flow

- **Opening a search**: show what Room has right away, and call `refresh` in the background every time. There is no cache timeout.
- **Search text**: waits 300 ms after the last keystroke before searching.
- **Filters**: applied when the reader taps "Show N recipes".
- **"Show N recipes"**: calls `count` 300 ms after the last filter change. When `count` returns `null`, the button says "Show recipes".
- **Load more**: when the last items come into view, `hasMore` is true and no load is running.
- **Pull-to-refresh**: on the list, calls `refresh`. The saved list stays visible under the indicator.
- **"Try again"** (Error) and **"Refresh"** (Empty catalogue) call `refresh`.
- **No automatic retry.** Only the reader retries.

## Screen states

| Room has this search? | Last call | Screen |
|---|---|---|
| No | Running | Loading (DESIGN 3.1) |
| No | Offline | "You're offline. Results for this search aren't saved yet." + "Retry" |
| No | Generic | Error (DESIGN 3.6) |
| Yes, 0 recipes, catalog | `Success` | Empty catalogue (DESIGN 3.7) |
| Yes, 0 recipes, with filters or text | `Success` | No results (DESIGN 3.5) |
| Yes, has recipes | Running | List |
| Yes, has recipes | Offline | List + snackbar "You're offline. Showing saved recipes." · "Retry" |
| Yes, has recipes | Generic | List + snackbar "Couldn't refresh recipes." · "Retry" |

Load more failing: the "Loading more recipes…" row becomes "Couldn't load more" + "Retry". The cards stay.

## Versions

- Room **2.8.5** (`androidx.room`), not Room 3.
- kotlinx.serialization follows the Kotlin version of the build: 1.12.0 with Kotlin 2.4.x, otherwise 1.11.0. The foundation slice picks it.
- Retrofit 3.0.0 with the kotlinx converter, OkHttp and logging-interceptor 5.5.0.

## Not in this version

- Monitoring: an `ApiCallEvent` per call sent to a dashboard. The call outcomes above are ready for it.
- Offline results for a search never made: filter the saved recipes locally and say the result may be incomplete.
