# Project guidelines

How code is written and changed in this repo. What to build is in [specs/](specs/), how it looks in [DESIGN.md](DESIGN.md), the words in [GLOSSARY.md](../GLOSSARY.md).

## Language

- Everything written is in English: code, comments, docs, commits, issues, pull requests and UI copy.
- Keep it simple: short sentences, common words.
- Use the words from the glossary in code, UI and docs. A new domain word goes into `GLOSSARY.md` in the same change.

## Kotlin

- Official Kotlin style. detekt and lint decide the rest ([quality.md](specs/quality.md)).
- `internal` by default. Only what [architecture.md](specs/architecture.md) lists as public (models, use cases, navigation functions, Koin modules, the clients `:core:network` builds for other modules, and the `:core:ui` theme, components and formatters) is `public`.
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
- Keep each pull request small enough to review in one sitting (about 400 changed lines, not counting generated files). A ticket can take several pull requests.
- Split a ticket into a **stack**: each part is one branch, `impl/<issue>-<part>-<topic>` (`impl/18-1-build-logic`, `impl/18-2-theme`), made from the part before it. The first part comes from `main`. Each part builds and passes the checks on its own.
- Every PR in a stack says its place in the PR body: "Part 2 of 3 for #18, based on #31".
- The repo does not delete merged branches. Before merging a part, retarget its PR to `main` if the part below it is already merged.
- The same goes across tickets: a ticket that starts before the previous one is merged stacks on it.
- Commits follow Conventional Commits: `feat:`, `fix:`, `test:`, `refactor:`, `build:`, `ci:`, `docs:`.
- When a PR changes the UI in a way a reviewer would want to see, ask the human before opening it whether it needs evidence: screenshots, a video, or none. When it does, record it on the emulator (`adb exec-out screencap -p > shot.png`, `adb shell screenrecord /sdcard/demo.mp4`), light and dark when colors changed, and give the human the files to attach to the PR. Keep them out of the repo.
- The PR body says what changed and how it was checked. Parts end with `Part of #<issue>`, and only the last part ends with `Closes #<issue>`.

## Done

A ticket is done when:

- `./gradlew detekt lint test assembleDebug` is green.
- The tests the ticket asks for exist, the pure rules written test first ([quality.md](specs/quality.md#test-first)).
- The app runs and shows what the ticket's "Done when" says.
- Docs still match the code: when the code proves a spec wrong, the spec is fixed in the same PR. A hard to reverse, surprising choice between real options gets an ADR in [adr/](adr/).
