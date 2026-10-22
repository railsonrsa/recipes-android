# Quality spec

The checks every change must pass, and how the code is tested. Modules are in [architecture.md](architecture.md), domain rules in [domain.md](domain.md), data rules in [data.md](data.md). Decided in issue #7.

## Checks

| Check | Rules | Fails the build |
|---|---|---|
| detekt | Default rules, plus the two Compose changes from the detekt docs ([config/detekt/detekt.yml](../../config/detekt/detekt.yml)) | Yes, on any issue |
| Android lint | Default rules | Yes, on any issue |
| Unit tests | `./gradlew test` | Yes |

- No baseline. The code starts clean and stays clean.
- Lint warnings fail the build too. Only the lint checks that ask for newer versions are off (`AndroidGradlePluginVersion`, `GradleDependency`, `NewerVersionAvailable`, `OldTargetApi`): versions and SDK levels are pinned on purpose ([architecture.md](architecture.md#build)), and these checks would break the build each time a library or an Android version ships. CI already has a newer SDK than `targetSdk 36`.
- A `@Suppress` needs a comment that says why.

Before each commit, run:

```
./gradlew detekt lint test
```

Red means don't commit.

## CI

One GitHub Actions workflow. It runs on every pull request and on every push to `main`.

```
./gradlew detekt lint test assembleDebug
```

- JDK 17, Gradle cache.
- `assembleDebug` checks that the app builds, including the `debug` source set (the Network menu).
- No emulator. No instrumented tests.
- On failure, the reports (`build/reports/`, `build/test-results/`) are uploaded as a workflow artifact.

## Test libraries

- JUnit 4
- Truth for assertions
- Turbine for Flows
- kotlinx-coroutines-test
- Robolectric, to run Room on the JVM
- MockK

## Test doubles

| Use | When | Example |
|---|---|---|
| MockK | A simple dependency you stub or verify | A use case calls the repository with this query |
| Hand-written fake | A dependency with state | `FakeRecipeRepository` in ViewModel tests: its Flows change over time |
| The real thing | Room, OkHttp, Retrofit in `:recipes:data` | In-memory Room database, the fake interceptors from [data.md](data.md#fake-endpoint) |

Never mock Room, OkHttp or Retrofit.

## Test first

Write the failing test first for the pure rules:

- `SearchQuery`: text normalization, a word moving between included and excluded, the active filter count.
- `FakeRecipeSearch.run`: every matching rule in [domain.md](domain.md#search), paging and `pagination`.
- The `Duration` formatter: 30s, 1m, 1m 30s, 1h, 1h 30m.
- Servings scaling, and the quantity formatter: ¼, ½, ¾, 1½, pinches and cups.

Everywhere else, tests come in the same change as the code.

## What is tested where

| Module | Tests |
|---|---|
| `:recipes:domain` | `SearchQuery` rules, servings scaling. Use cases only forward the call, so they have no tests |
| `:recipes:data` | `FakeRecipeSearch.run`. DTO → domain mappers. The repository with in-memory Room, real Retrofit and OkHttp and the fake interceptors: the `CallOutcome` for each fake network mode, and the refresh and next-page transactions |
| `:core:network` | `FakeNetworkModeInterceptor`: each mode throws the right exception or answers the right status |
| `:core:ui` | The `Duration` formatter |
| `:feature:recipe-list`, `:feature:recipe-detail` | The quantity formatter (detail). ViewModels with `FakeRecipeRepository`, real use cases and Turbine: each `XAction` gives the expected `XUiState` and `XEffect` |
| `:app` | None |

## Not in this version

- ktlint formatting (detekt-formatting) and Compose rules for detekt.
- Stricter detekt and lint configs.
- A minimum test coverage (Kover).
- Screenshot tests (Roborazzi).
- Compose UI tests.
