# Recipes Android

A native Android app to browse, search and filter recipes. Kotlin, Jetpack Compose, Material 3, offline-first, modular. A local JSON asset plays the HTTP server.

## Work

1. Take one implementation ticket (GitHub issue, child of the implementation map). One ticket per session.
2. Read the docs its "Specs" section points to before writing code.
3. Build it test first for the pure rules, the rest with tests in the same change.
4. Check: `./gradlew detekt lint test assembleDebug`. Red means not done.
5. Open small pull requests, stacked when the ticket is big, as in [guidelines](docs/guidelines.md#git-and-pull-requests). Never push to `main`.

The ticket is done when [Done](docs/guidelines.md#done) holds.

## Docs

| When you | Read |
|---|---|
| Name anything in code, UI or docs | [GLOSSARY.md](GLOSSARY.md) |
| Build or change a screen, component or text | [docs/DESIGN.md](docs/DESIGN.md) |
| Touch `Recipe`, search, filters, time or servings | [docs/specs/domain.md](docs/specs/domain.md) |
| Touch the endpoint, the fake interceptors, Room, paging, refresh or errors | [docs/specs/data.md](docs/specs/data.md) |
| Add a module, a use case, a screen, navigation or DI | [docs/specs/architecture.md](docs/specs/architecture.md) |
| Write tests or touch detekt, lint or CI | [docs/specs/quality.md](docs/specs/quality.md) |
| Need search cases or edge cases from the asset | [docs/specs/test-data.md](docs/specs/test-data.md) |
| Write code, commit or open a PR | [docs/guidelines.md](docs/guidelines.md) |
| Wonder why a choice was made | [docs/adr/](docs/adr/), [docs/research/](docs/research/) |

## Rules

- The docs are the source of truth. When they disagree with the code, the docs win; when the code proves them wrong, fix the docs in the same PR.
- DESIGN.md section 6 and each spec's "Not in this version" list what not to build.
- Everything is written in English, kept simple.

## Reviews

When the human asks you to review a pull request or to answer their review comments, you post from their GitHub account.

- End every review, comment or reply you post with one last line of its own: 🤖 Written by Claude
- After each change made for a review comment, read the PR body again. When it shows an output the change altered (a format, an example, a count), update the PR body in the same round.
