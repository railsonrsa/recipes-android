# Project guidelines

How code is written and changed in this repo. What to build is in [specs/](specs/), how it looks in [DESIGN.md](DESIGN.md), the words in [GLOSSARY.md](../GLOSSARY.md).

## Language

- Everything written is in English: code, comments, docs, commits, issues, pull requests and UI copy.
- Keep it simple: short sentences, common words.
- Use the words from the glossary in code, UI and docs. A new domain word goes into `GLOSSARY.md` in the same change.

## Kotlin

- Official Kotlin style. detekt and lint decide the rest ([quality.md](specs/quality.md)).
- `internal` by default. Only what [architecture.md](specs/architecture.md) lists as public (models, use cases, navigation functions, Koin modules) is `public`.
- No `!!`. Model the missing case instead (a nullable type, an empty list, a state).
- UI state and domain models are immutable `data class`es with `val`s and read-only collections.
- Room and Retrofit are main-safe. Call them from `viewModelScope` without `withContext`.
- One comment line when the code can't say why. No comments that repeat the code.

## Compose

- Colors, type, shapes and spacing come only from the theme ([DESIGN.md](DESIGN.md#1-tokens)). No hex, `sp` or `dp` literals in screens, except the sizes DESIGN.md lists.
- UI text lives in `strings.xml` (plurals in `<plurals>`). No string literals on screen.
- Every stateless composable (the Layout and each state) has a `@Preview` in light and dark.
- Composables take state and lambdas, never the ViewModel. Only `XScreen` sees the ViewModel.

## Tests

- Test names are sentences in backticks: `` `excluded word moves to included when added there` ``.
- One behavior per test. Arrange, act, assert, separated by a blank line.
- The cases for search come from [test-data.md](specs/test-data.md).

## Git and pull requests

- Never commit or push to `main`. Every change goes through a pull request, and the human merges it.
- One branch per implementation ticket: `impl/<issue>-<topic>`, from `main`. If the previous ticket is not merged yet, branch from it and say so in the PR, so it can be retargeted to `main` before merging. The repo does not delete merged branches.
- Commits follow Conventional Commits: `feat:`, `fix:`, `test:`, `refactor:`, `build:`, `ci:`, `docs:`.
- The PR body says what changed and how it was checked, and ends with `Closes #<issue>`.

## Done

A ticket is done when:

- `./gradlew detekt lint test assembleDebug` is green.
- The tests the ticket asks for exist, the pure rules written test first ([quality.md](specs/quality.md#test-first)).
- The app runs and shows what the ticket's "Done when" says.
- Docs still match the code: when the code proves a spec wrong, the spec is fixed in the same PR. A hard to reverse, surprising choice between real options gets an ADR in [adr/](adr/).
