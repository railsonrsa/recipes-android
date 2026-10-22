# Cache server results per search, don't re-run filters locally

The app caches, for each search, the list of recipes the server returned (`search_results` keyed by `searchKey`), and never re-runs the filters on the saved recipes. Which recipes match a search belongs to the server: copying the filter logic into the app would give two sources of results that can drift apart. The cost is that a search never made before shows nothing offline, even when its recipes are already saved. Issue #4.

## Considered options

- **Filter the saved recipes locally** (SQL or Kotlin): works offline for any search, but duplicates the server's rules and can show a partial result as if it were complete. Kept as a "would do with more time" fallback, clearly labeled as possibly incomplete.
- **Paging 3 `RemoteMediator`**: the official pattern for the same cache shape, but still `@ExperimentalPagingApi` and too many parts for 10 recipes. We page manually.
