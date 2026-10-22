# Domain spec

The rules of the recipes domain. Terms are defined in [GLOSSARY.md](../../GLOSSARY.md). Decided in issue #3.

## Model

One `Recipe` model serves both the list and the detail screen. There is no separate summary model.

| Field | Type | Notes |
|---|---|---|
| `id` | `String` | UUID from the backend |
| `title` | `String` | |
| `description` | `String` | |
| `servings` | `Int` | As written in the recipe |
| `prepTime` | `Duration` | |
| `cookTime` | `Duration` | Zero means no cooking |
| `difficulty` | `Difficulty?` | `null` when unknown |
| `cuisine` | `String` | |
| `rating` | `Double?` | 0 to 5, `null` when there are no ratings |
| `imageUrl` | `String?` | |
| `dietaryTags` | `Set<DietaryTag>` | Can be empty |
| `ingredients` | `List<Ingredient>` | |
| `instructions` | `List<Step>` | Ordered by step number |

- `Ingredient(name: String, quantity: Double, unit: String?)`
- `Step(number: Int, description: String, duration: Duration?)`
- `Difficulty`: `Easy`, `Medium`, `Hard`. The backend sends `1`, `2`, `3`. Any other value maps to `null`, and the UI hides it.
- `DietaryTag`: `Vegan`, `Vegetarian`, `Pescatarian`, `Halal`, `GlutenFree`, `DairyFree`, `NutFree`, `LowCarb`. The mapper drops unknown keys, so there is no `Unknown` value in the domain.

Derived values:

- `totalTime = prepTime + cookTime`
- `instructionsTime` = the sum of the non-null step durations

## Time

- Every time value from the backend is in **seconds**: `prepTimeSeconds`, `cookTimeSeconds`, `durationSeconds`. The domain holds `kotlin.time.Duration`.
- One formatting util turns a `Duration` into text. It shows at most **2 units**, the largest first, skips zero units and drops the rest:
  - 30 s → "30 s"
  - 90 s → "1 min 30 s"
  - 3600 s → "1 h"
  - 3630 s → "1 h" (the seconds are dropped)
  - 5400 s → "1 h 30 min"
- A cook time of zero shows "None".

## Servings scaling

- The reader picks the scaled servings, from 1 to 20.
- The domain scales every ingredient quantity by `scaledServings / servings`. This includes `pinch` and quantities with no unit.
- The UI formats the result: ¼, ½, ¾, 1½, otherwise up to 1 decimal place, and "pinch" or "pinches".

## Search

A `SearchQuery` is the search text plus the filters. An empty query returns the whole catalog.

| Part | Meaning | A recipe matches when |
|---|---|---|
| Search text | Free text | Its title or description contains the text |
| Dietary tags | Set | It has **all** the selected tags |
| Servings range | Any, 1–2, 3–4, 5–6, 7+ | Its `servings` (not the scaled servings) is in the range |
| Difficulties | Set | Its difficulty is **any** of the selected ones. A recipe with no difficulty never matches a difficulty filter. |
| Included ingredients | Set of words | Every word matches some ingredient name |
| Excluded ingredients | Set of words | No word matches any ingredient name |
| Instruction search | Text | Some step description contains the text |

Text matching rules, the same for every text above:

- "Contains" match, case-insensitive and accent-insensitive (normalize with NFD and strip the marks). "pao" matches "Pão", "garlic" matches "Garlic cloves, minced".
- Blank words are ignored. Duplicates are removed.
- A word can't be both included and excluded. Adding it to one side removes it from the other.

**Active filter count** (the badge) is part of `SearchQuery`: 1 for each dietary tag, each difficulty, each included or excluded ingredient, 1 for a servings range other than Any, and 1 for a non-blank instruction search. The search text does not count.

## Search result

The domain does not know about pages. A `SearchResult` holds the recipes that match a search so far and `hasMore`. Pages, page numbers and page size stay in the data layer.

## Changes to DESIGN.md

DESIGN.md is still the UI source of truth. These decisions override it:

- The "Method" section on the detail screen is called **"Instructions"**.
- Step durations are `durationSeconds`, not `durationMinutes`. Prep and cook times are in seconds too.
- The filter sheet gets excluded ingredients and an **Instructions** section. See [Filter sheet: excluded ingredients and instructions](#filter-sheet-excluded-ingredients-and-instructions).

### Filter sheet: excluded ingredients and instructions

Decided in issue #6 (variant A of the prototype).

**Ingredients section**

- A `SingleChoiceSegmentedButtonRow` with two segments, **Must have** and **Must not have**, sits above the field. It picks where the next word goes. It starts on Must have and is not a filter itself (it does not count in the badge).
- One text field and one add button, as in DESIGN.md. The placeholder follows the segment: "Search ingredients" or "Ingredient to avoid".
- Included and excluded chips share one `FlowRow`: included first, then excluded.
- Included chip: as in DESIGN.md ("Coconut milk ×", `vegetarian` container).
- Excluded chip: `InputChip` with a 1 dp `outline` border on `surfaceContainerLowest`, a leading block icon (18 dp) and the label "No garlic".
- Adding a word that is on the other side moves it, and a snackbar says so: "“garlic” moved to Must have".

**Instructions section** (after Ingredients)

- One `OutlinedTextField`, no add button: leading numbered-list icon, placeholder "Search steps, e.g. oven", trailing clear icon when it has text.
- Typing updates the "Show N recipes" count (the same 300 ms wait as the other filters).

**Active filters on the list**

- Included and excluded chips look the same as in the sheet.
- The instruction search is one chip: `secondaryContainer`, leading numbered-list icon, label "Steps: oven".
- Chip order: diet, servings, difficulty, included, excluded, instructions.
