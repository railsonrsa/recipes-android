# Architecture spec

How the app is split into modules, what each module exposes, and how a screen is built. Terms are in [GLOSSARY.md](../../GLOSSARY.md). Data rules are in [data.md](data.md). Decided in issue #5.

## Modules

Seven modules. They sit side by side in the repo. The folders `core/`, `recipes/` and `feature/` only group them.

```
recipes-android/
├── app/                    :app
├── build-logic/            convention plugins
├── core/
│   ├── ui/                 :core:ui
│   └── network/            :core:network
├── recipes/
│   ├── domain/             :recipes:domain
│   └── data/               :recipes:data
└── feature/
    ├── recipe-list/        :feature:recipe-list
    └── recipe-detail/      :feature:recipe-detail
```

Package prefix: `com.railson.recipes`.

| Module | Role | Packages |
|---|---|---|
| `:app` | `MainActivity`, `NavHost`, starts Koin. The debug "Network" menu lives in its `debug` source set | |
| `:core:ui` | Material 3 theme, DESIGN.md tokens, shared components, the `Duration` → text formatter | `theme/`, `components/`, `format/`, `preview/` |
| `:core:network` | `OkHttpClient`, `Retrofit`, `Json`, `HttpLoggingInterceptor`, `FakeNetworkConfig`, `FakeNetworkModeInterceptor` | `client/`, `fake/`, `di/` |
| `:recipes:domain` | Pure Kotlin. `Recipe` and its types, `SearchQuery` and its text normalization (shared by the move rule, the fake server and the cache key), `SearchResult`, `CallOutcome`, the `RecipeRepository` interface, the use cases | `model/`, `repository/`, `usecase/` |
| `:recipes:data` | Room, `RecipesApi` (Retrofit), DTOs, `FakeRecipesInterceptor`, `FakeRecipeSearch`, the asset JSON, `RecipeRepository` implementation | `remote/`, `fake/`, `local/{database,dao,entity}`, `mapper/`, `repository/`, `di/` |
| `:feature:recipe-list` | List screen and filter sheet | `navigation/`, `list/`, `filter/`, `components/`, `di/` |
| `:feature:recipe-detail` | Detail screen | `navigation/`, `detail/`, `components/`, `di/` |

What goes in `:core:network` and what stays in `:recipes:data`: anything that works for **any** API goes in `:core:network`. Anything that only makes sense for recipes stays in `:recipes:data`.

## Dependency rules

```
:app ──▶ every module

:feature:recipe-list   ─┐
:feature:recipe-detail ─┴─▶ :recipes:domain, :core:ui

:recipes:data ──▶ :recipes:domain, :core:network
```

- A feature only sees `:recipes:domain` and `:core:ui`. A feature never depends on another feature.
- `:recipes:data` and `:core:network` are never seen by a feature. Only `:app` wires them.
- In `:recipes:data` everything is `internal`, except its Koin module.
- In a feature everything is `internal`, except its navigation functions, its route and its Koin module.

These rules are kept by review. No tool checks them yet.

## `:recipes:domain` is pure Kotlin

It is a Kotlin/JVM module with no Android plugin. Inside it:

- no `android.*`;
- no `java.*` (no `java.util.UUID`, no `java.time`). Use `String`, `kotlin.time.Duration`, and so on. One exception: `normalizedForSearch()` uses `java.text.Normalizer` to strip accents;
- only libraries that also exist for Kotlin Multiplatform: `kotlinx.coroutines`.

So the module could move to Kotlin Multiplatform later with one change: `normalizedForSearch()` becomes an `expect` function with one `actual` per platform. The app stays native Android.

## Public use cases

`:recipes:domain` exposes one use case per repository operation. Features only call use cases, never the repository.

| Use case | Calls |
|---|---|
| `ObserveSearchUseCase` | `observeSearch(query)` |
| `RefreshSearchUseCase` | `refresh(query)` |
| `LoadNextPageUseCase` | `loadNextPage(query)` |
| `CountRecipesUseCase` | `count(query)` |
| `ObserveRecipeUseCase` | `observeRecipe(id)` |

The list and the detail screen both use `Recipe`. Each one renders it its own way.

## Screens

Each screen has these parts, named after the screen (`RecipeList…`, `RecipeDetail…`):

| Part | What it does |
|---|---|
| `XScreen` | Stateful. Gets the ViewModel with `koinViewModel()`, collects state and effects, passes actions down |
| `XLayout` | Stateless, with previews. Picks which state to draw |
| One composable per state | `XLoading`, `XList`, `XError`, `XEmpty`, … Each is stateless, with a preview |
| `XViewModel` | Takes `XAction`s, exposes `StateFlow<XUiState>` and a `Channel<XEffect>` |
| `XUiState` | One state for the whole screen. A sheet with a life of its own gets its own state next to it: `RecipeFiltersUiState` holds the applied filters and the sheet's draft, since they show in every list state |
| `XAction` | What the reader does: types text, taps a card, pulls to refresh, … |
| `XEffect` | Things that happen once: show a snackbar, navigate |

The ViewModel keeps nothing it can't read again from Room. `SavedStateHandle` is only for throwaway screen state.

## Navigation

- One Activity. Navigation Compose with `@Serializable` routes.
- Each feature exposes a `NavGraphBuilder` function and its route. `:app` connects them.
- The list doesn't know the detail screen exists. It calls `onRecipeClick(id)`, and `:app` navigates to `RecipeDetailRoute(id)`. Any future screen that shows a recipe does the same.
- The detail screen is a full screen, not a sheet.
- The filter sheet is a bottom sheet inside the list feature. It is not a route.

```kotlin
// :feature:recipe-list
fun NavGraphBuilder.recipeListScreen(onRecipeClick: (String) -> Unit, topBarActions: @Composable RowScope.() -> Unit)

// :feature:recipe-detail
@Serializable data class RecipeDetailRoute(val id: String)
fun NavGraphBuilder.recipeDetailScreen(onBack: () -> Unit)
```

### Recipe not in Room

The detail screen reads only Room, and the API has no route for one recipe. If a screen opens the detail with an id that was never saved, the detail shows a **"Recipe not available"** state with a back button. Today the list only shows saved recipes, so this doesn't happen, but the detail module handles it.

## Debug network menu

The ⋮ "Network" menu ([data.md](data.md#fake-network-modes)) exists only in debug builds. The list screen takes a `topBarActions` slot. The `debug` source set of `:app` fills it with the menu, which switches `FakeNetworkConfig` in `:core:network`. The release build passes an empty slot. The list feature doesn't know the fake exists.

The fake interceptors are in release builds too. There is no real server.

## DI

Koin. Each module that provides objects has a Koin module in `di/`. `:app` starts Koin with all of them.

## Build

- Version catalog and convention plugins in `build-logic`: `android-library`, `android-feature`, `jvm-library`, plus `android-compose` (used by `:core:ui` and the features) and `android-application` (used by `:app`).
- `minSdk 26`, `compileSdk 36`, `targetSdk 36`. JDK 17.
- AGP 8.13 and Kotlin 2.4. AGP 9 waits for a detekt release that supports it. AndroidX libraries stay on the last versions that build with AGP 8 and `compileSdk 36` (Compose BOM 2026.06.01, lifecycle 2.10, navigation 2.9).
- No product flavors.

## Not in this version

- A Koin `verify()` test of the graph.
- A tool that checks the dependency rules.
