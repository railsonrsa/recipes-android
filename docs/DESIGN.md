# Recipes app: Design spec

Source of truth for the UI of the Android recipes app (Kotlin, Jetpack Compose, Material 3).
Read this before building any screen. Build only what is described here.

The decisions from issues #3 (domain) and #6 (filter sheet) are already applied here. Domain rules behind the UI (matching, scaling, time) are in [specs/domain.md](specs/domain.md).

## 0. Rules for implementation

- Use **Material 3 components** (`androidx.compose.material3`) as-is, styled through the theme. Do not hand-build a component that M3 already has (a chip is a `FilterChip` / `InputChip`, not a `Row` with a border).
- All colors, type, shapes and spacing come from the theme tokens in section 1. No hard-coded hex values, sp or dp values inside screens, except the few documented in section 2.
- The app is **edge-to-edge**. Handle insets with `WindowInsets` (`statusBars`, `navigationBars`) and `Scaffold`; never hard-code status bar heights.
- Light and dark themes are both required (section 1.1). The theme follows the system setting.
- UI copy is in **English**.
- Anything listed in section 6 (Out of scope) must **not** be built.

---

## 1. Tokens

Only the tokens below exist. If a screen seems to need another token, reuse the closest one; do not add new ones.

### 1.1 Colors

Mapped to M3 `ColorScheme` roles. Roles not listed keep the M3 defaults and must not be used by screens.

| Role | Light | Dark | Used for |
|---|---|---|---|
| `background` / `surface` | `#F7F4EE` | `#17140F` | Screen background |
| `onSurface` / `onBackground` | `#1B1710` | `#F3EEE6` | Primary text, icons |
| `onSurfaceVariant` | `#625B53` | `#B9B0A4` | Secondary text: metadata, counts, descriptions, labels |
| `surfaceContainerLowest` | `#FFFFFF` | `#221E18` | Cards, search bar, bottom sheet, detail content panel, text field |
| `surfaceContainer` | `#F9F4E8` | `#2E2922` | Round icon button fill, image placeholder, step number circle, empty/error icon circle |
| `outline` | `#8A8379` | `#8A8379` | Borders of unselected chips, segmented buttons, outlined text field, outlined button, servings stepper |
| `outlineVariant` | `#DAD5CE` | `#4A443C` | Search bar border, stat box border, card "+N" chip border |
| `primary` | `#FE6F0C` | `#FE6F0C` | Filled buttons, filter badge, stepper glyphs, timer icon |
| `onPrimary` | `#1B1710` | `#1B1710` | Label on filled buttons and badge (white fails contrast, so it is ink on purpose) |
| `primaryContainer` | `#FEE3CE` | `#553621` | Tonal "add ingredient" icon button fill |
| `onPrimaryContainer` | `#FE6F0C` | `#FE6F0C` | Glyph in the tonal add button |
| `secondaryContainer` | `#B8D4FF` | `#23395A` | Selected filter chip and selected segment |
| `onSecondaryContainer` | `#1B1710` | `#F3EEE6` | Label and check icon on the selected chip or segment |
| `tertiary` | `#256DCB` | `#8FB8F2` | Section headers ("Diet", "Ingredients", "Instructions") and the cuisine label |
| `scrim` | `#000000` | `#000000` | Bottom sheet scrim (32% light, 50% dark) |

**Extended colors.** These go in a small `@Immutable data class RecipeColors` provided through a `CompositionLocal`, with a light and a dark instance:

| Token | Light | Dark | Used for |
|---|---|---|---|
| `actionText` | `#BA4D01` | `#FF9A52` | `TextButton` content ("Clear all", "Clear all filters"). The `primary` orange fails contrast as text. |
| `hairline` | `#F0EDE5` | `#2F2A23` | 1 dp dividers between rows and sections, skeleton blocks, progress track |
| `ratingStar` | `#FFD91B` | `#FFD91B` | Filled star in list cards only (the detail screen uses an outlined `onSurface` star) |
| `dietTags` | see 1.2 | see 1.2 | Diet tag container colors, one per tag (`dietTags.vegan`, …). `:core:ui` does not see the domain, so features pick the color for each `DietaryTag` |
| `spinner` | `#4E8DDF`, `#FE6F0C`, `#80C532`, `#A259CC` | same | The loading arc cycles through these four, in order |

### 1.2 Diet tag colors

Each dietary attribute has a fixed container color. Text on it is `onSurface`. The color code never carries meaning alone, because the tag always shows its label.

| Key (JSON) | Label | Light | Dark |
|---|---|---|---|
| `vegetarian` | Vegetarian | `#D4ECB8` | `#34492A` |
| `vegan` | Vegan | `#9AD15B` | `#3E5A1C` |
| `gluten-free` | Gluten-free | `#FFEE8A` | `#4D4413` |
| `dairy-free` | Dairy-free | `#B8D4FF` | `#23395A` |
| `nut-free` | Nut-free | `#FEC79D` | `#573A22` |
| `low-carb` | Low-carb | `#D9BFF5` | `#3F2F57` |
| `halal` | Halal | `#FEE3CE` | `#553621` |
| `pescatarian` | Pescatarian | `#C4E6EF` | `#1F4550` |

Unknown keys are dropped when the data is mapped, so they are never shown.

**Display order** (the most specific first): vegan, vegetarian, pescatarian, halal, gluten-free, dairy-free, nut-free, low-carb. List cards show the first 2 tags plus a `+N` chip. The detail screen shows all of them.

Ingredient chips in the filters ("Coconut milk ×") use the `vegetarian` light-green container.

### 1.3 Typography

Two families, bundled in `res/font` (both are Google Fonts under the OFL):
- **Figtree** (400, 500) replaces Roboto for every text role below, except the two serif roles.
- **Young Serif** (400) is used for `titleLarge` and `headlineSmall` only.

Only these M3 styles are defined and used:

| M3 style | Family | Size / line height | Weight | Tracking | Used for |
|---|---|---|---|---|---|
| `headlineSmall` | Young Serif | 24 / 32 | 400 | 0 | Recipe title on detail |
| `titleLarge` | Young Serif | 22 / 28 | 400 | 0 | Top app bar titles, sheet title, empty and error headings |
| `titleMedium` | Figtree | 16 / 24 | 500 | 0.15 | Recipe card title |
| `titleSmall` | Figtree | 14 / 20 | 500 | 0.1 | Section headers (in `tertiary`), stat box values |
| `bodyLarge` | Figtree | 16 / 24 | 400 | 0.5 | Search text, text field, ingredient rows, instruction steps |
| `bodyMedium` | Figtree | 14 / 20 | 400 | 0.25 | Card metadata line, descriptions, state messages, "10 recipes" |
| `bodySmall` | Figtree | 12 / 16 | 400 | 0.4 | "Serves 4 · Italian", "Must match all", stat box labels, "That's all 10 recipes" |
| `labelLarge` | Figtree | 14 / 20 | 500 | 0.1 | Buttons, chips, segmented buttons, detail diet tags |
| `labelMedium` | Figtree | 12 / 16 | 500 | 0.5 | Diet tags in list cards, cuisine label |
| `labelSmall` | Figtree | 11 / 16 | 500 | 0.5 | Badge count |

### 1.4 Shapes

| M3 slot | Radius | Used for |
|---|---|---|
| `extraSmall` | 4 dp | Outlined text field |
| `small` | 8 dp | Chips, diet tags, list thumbnails |
| `medium` | 12 dp | Recipe cards, stat boxes |
| `extraLarge` | 28 dp | Bottom sheet top corners, detail content panel top corners |
| (full) | 50% | Search bar, buttons, segmented button row, servings stepper, icon buttons (M3 defaults) |

### 1.5 Spacing

A plain `object Spacing` (or a `CompositionLocal`, if preferred) with only these values:

| Token | Value | Used for |
|---|---|---|
| `xs` | 4 dp | Gap between card text lines, between tags in a card |
| `sm` | 8 dp | Gap between cards, between chips, between stat boxes |
| `md` | 12 dp | Card inner padding, section vertical padding in the sheet |
| `lg` | 16 dp | Screen side margins, gap between content blocks |
| `xl` | 24 dp | Empty/error state spacing, detail panel top padding |

Minimum touch target: 48 dp, the M3 default. Keep `minimumInteractiveComponentSize`.

---

## 2. Components

| Need | M3 component | Spec |
|---|---|---|
| Screen title | `TopAppBar` (small) | 64 dp, `titleLarge`. On detail, it appears only when scrolled (screen 10), with a back icon and the title on 1 line, ellipsized. |
| Search | `SearchBar` (docked) or `DockedSearchBar` | 56 dp, full radius, `surfaceContainerLowest` with a 1 dp `outlineVariant` border. Leading search icon 24 dp, `bodyLarge` placeholder "Search recipes". Trailing filter `IconButton` with a 40 dp `surfaceContainer` circle. A `Badge` (`labelSmall`, `primary`) shows the number of active filters. |
| Recipe card | `Card` (filled, `surfaceContainerLowest`) | Radius 12, padding 12, row layout: 80 dp thumbnail (radius 8) + text column (title `titleMedium` max 2 lines with ellipsis; metadata `bodyMedium`; "Serves N · Cuisine" `bodySmall`; tags row). Gap between cards 8. |
| Diet tag (list) | small `Surface` | 24 dp tall, radius 8, horizontal padding 8, `labelMedium`, diet color. `+N` uses the same shape with a 1 dp `outlineVariant` border and `onSurfaceVariant` text. |
| Diet tag (detail) | `AssistChip`-sized `Surface` | 32 dp, radius 8, padding 12, `labelLarge`, diet color. Not clickable. |
| Active filter (list header) | `InputChip` | 32 dp, trailing close icon 18 dp. Diet: diet color container. Included and excluded ingredients: as in the sheet. Servings: "3–4 servings". Difficulty: "Easy". Both outlined (1 dp `outline`). Instruction search: one chip "Steps: oven", `secondaryContainer`, leading `format_list_numbered` icon. Order: diet, servings, difficulty, included, excluded, instructions. Tapping a chip removes that filter and searches again. |
| Diet filter | `FilterChip` | 32 dp, radius 8. Unselected: 1 dp `outline` border. Selected: `secondaryContainer` with a leading check icon 18 dp. Multi-select, "must match all". |
| Servings filter | `SingleChoiceSegmentedButtonRow` | 40 dp, 5 segments: Any, 1–2, 3–4, 5–6, 7+. The selected segment shows the check icon. |
| Difficulty filter | `FilterChip` | Easy / Medium / Hard, multi-select. |
| Ingredient mode | `SingleChoiceSegmentedButtonRow` | 40 dp, 2 segments: **Must have** / **Must not have**. Sits above the ingredient field and picks where the next word goes. Starts on Must have. It is not a filter (it does not count in the badge). |
| Ingredient filter | `OutlinedTextField` + `FilledTonalIconButton` | Field: 56 dp, radius 4, leading search icon. Placeholder follows the mode: "Search ingredients" or "Ingredient to avoid". Button: 40 dp `primaryContainer` circle with a "+" glyph, inside a 48 dp target. Added words appear below as `InputChip`s that wrap (one `FlowRow`, gap 8): included first, then excluded. |
| Included ingredient chip | `InputChip` | "Coconut milk ×", `vegetarian` container, trailing close icon 18 dp. |
| Excluded ingredient chip | `InputChip` | "No garlic ×", `surfaceContainerLowest` with a 1 dp `outline` border, leading `block` icon 18 dp, trailing close icon 18 dp. |
| Instruction search | `OutlinedTextField` | 56 dp, radius 4, leading `format_list_numbered` icon, placeholder "Search steps, e.g. oven", trailing clear icon when it has text. No add button. |
| Filters container | `ModalBottomSheet` | Top radius 28, default drag handle (32×4 inside a 48 dp area), scrim at 32% / 50%. Header: "Filters" (`titleLarge`) + `TextButton` "Clear all" in `actionText`. Sections are separated by 1 dp `hairline` dividers inset 16. A sticky bottom area has a full-width `Button` "Show N recipes" (40 dp), above the navigation bar inset. |
| Primary action | `Button` (filled) | 40 dp, full radius, `labelLarge`, `primary` / `onPrimary`. |
| Secondary action | `OutlinedButton` | 40 dp, 1 dp `outline`, optional leading icon 18 dp. |
| Tertiary action | `TextButton` | 40 dp, `actionText` color. |
| Loading | `CircularProgressIndicator` | First load: 32 dp, stroke 4. Load more: 24 dp, stroke 3. Track `hairline`; the arc color cycles through `spinner` (`rememberInfiniteTransition`, about 3.6 s per full color cycle, plus the default rotation). |
| Stat box (detail) | `OutlinedCard` | 4 equal boxes in a row, gap 8, radius 12, 1 dp `outlineVariant`, vertical padding 12. Each box: 24 dp icon, value (`titleSmall`), label (`bodySmall`). |
| Servings stepper | custom `Row` in a bordered `Surface` | 40 dp, full radius, 1 dp `outline`. `IconButton` minus / plus with 20 dp glyphs in `primary`; "Serves N" in `labelLarge`. |
| Step duration | chip-shaped `Surface` (an `AssistChip` is always clickable) | 32 dp, radius 8, 1 dp `outlineVariant`, leading timer icon 18 dp in `primary`, `labelLarge` text. Hidden when the step has no duration. |

**Icons:** outlined, 24 dp, `onSurface`. Material Symbols Outlined equivalents:

| Use | Icon |
|---|---|
| Search | `search` |
| Filters | `tune` |
| Back | `arrow_back` |
| Close / remove | `close` |
| Add | `add` |
| Remove | `remove` |
| Check | `check` |
| Prep time | `timer` |
| Cook time | `local_fire_department` |
| Rating | `star` (outlined on detail, filled `ratingStar` in cards) |
| Image placeholder | `restaurant` |
| Error | `error` |
| Refresh | `refresh` |
| Excluded ingredient | `block` |
| Instruction search | `format_list_numbered` |

**Difficulty icon:** a custom 24 dp drawable with 3 vertical bars (heights 7, 12 and 18 dp, width 4, gap 2, radius 2). Easy fills 1 bar, Medium 2, Hard 3. Filled bars are `onSurface`, empty bars `outlineVariant`. It always appears next to the text label. In the card metadata line, the bars and the star are sized to the line height of the text, so they scale with the font.

---

## 3. Screens

The phone reference is 360×760 dp, edge-to-edge. Side margins are 16 dp everywhere.

### 3.1 First load (state `Loading`)
- Top app bar "Recipes".
- Search bar shown disabled (38% alpha). When it already has text (a new search loading), it stays enabled, so the reader can keep typing.
- Centered: 32 dp multicolor progress indicator + "Loading recipes…" (`bodyMedium`, `onSurfaceVariant`), 16 dp apart.

### 3.2 List (state `Content`)
- Top app bar "Recipes", then the search bar (16 dp margins).
- "10 recipes" (`bodyMedium`, `onSurfaceVariant`), padding 16 / 16 / 8.
- `LazyColumn` of recipe cards, gap 8, horizontal padding 16.
- Card metadata line: `★ 4.7 · 32m · Medium` (star filled `ratingStar`; rating in `onSurface` 500). When the rating is null, show "No ratings". When the difficulty is unknown, leave it out of the line. The time is the total time, formatted as in section 4.

### 3.3 Loading the next page
- The search bar stays pinned at the top (8 dp vertical padding, a 1 dp `hairline` below it).
- The last cards, then one skeleton card (`hairline` blocks in the card layout), then a row with a 24 dp progress indicator + "Loading more recipes…".

### 3.4 End of list
- After the last card: a centered "That's all 10 recipes" (`bodySmall`) between two 1 dp lines. No more requests are made (`hasNext = false`; a page-2 request returns `[]`).

### 3.5 Search with no results (state `NoResults`)
- The active filters appear as `InputChip`s under the search bar. The filter button badge shows the count.
- Centered illustration (decorative), "No recipes match" (`titleLarge`), an explanation (`bodyMedium`), and a `TextButton` "Clear all filters". The button shows only when filters are on. It keeps the search text, which the reader clears in the search bar.

### 3.6 Error (state `Error`)
- Search bar disabled, unless it has text or filters: then the reader can change the search. The filter button follows the bar.
- 72 dp `surfaceContainer` circle with a 36 dp error icon, "Couldn't load recipes" (`titleLarge`), "Something went wrong reading the recipe data." (`bodyMedium`), and a filled `Button` "Try again".

### 3.7 Empty catalogue (JSON `data: []` on first load)
- No search bar and no filters, since there is nothing to filter.
- Illustration, "No recipes yet" (`titleLarge`), "The recipe book is empty right now. New recipes will show up here." (`bodyMedium`), and an `OutlinedButton` "Refresh" with a refresh icon.

### 3.8 Filters (`ModalBottomSheet`)
Sections in this order, each with a `titleSmall` header in `tertiary`:
1. **Diet:** 8 `FilterChip`s in a `FlowRow`. "Must match all" (`bodySmall`) appears on the right of the header.
2. **Servings:** segmented button (Any / 1–2 / 3–4 / 5–6 / 7+).
3. **Difficulty:** 3 `FilterChip`s.
4. **Ingredients:** the Must have / Must not have segmented button, then the text field + add button, then the included and excluded chips. Adding a word that is on the other side moves it, and a snackbar says so: "“garlic” moved to Must have".
5. **Instructions:** the instruction search field. Typing updates the "Show N recipes" count (the same 300 ms wait as the other filters).

The bottom button shows a live count: "Show N recipes", or "Show 1 recipe" for one. The count runs right away when the sheet opens. While a new count loads, the button keeps the last number; when the count fails, it says "Show recipes". The sheet edits a draft: closing it without "Show N recipes" drops the changes, and "Clear all" clears only the draft.

### 3.9 Detail, top
- Hero image, 232 dp tall, edge-to-edge under the status bar. A back `IconButton` with a 40 dp `surfaceContainerLowest` circle sits over the image.
- A content panel overlaps the image by 28 dp (top radius 28, `surfaceContainerLowest`), with padding 24 top and 16 sides:
  - Cuisine (`labelMedium`, `tertiary`), then the title (`headlineSmall`).
  - Stat boxes: Prep, Cook, Level, Rating.
  - Diet tags (`FlowRow`, gap 8).
  - Description (`bodyMedium`, `onSurfaceVariant`).
  - A divider, then the "Ingredients" header row (56 dp) with the servings stepper on the right.
  - Ingredient rows: min height 48, name in `bodyLarge`, quantity on the right in `bodyMedium` `onSurfaceVariant`, `hairline` divider below.

### 3.10 Detail, scrolled to the instructions
- The collapsed top app bar appears (back icon + title, 1 line).
- The ingredient list continues.
- "Instructions" header row (56 dp) with "N steps · X" (`bodySmall`) on the right. X is the sum of the non-null step durations, formatted as in section 4. With no durations at all, only "N steps".
- Each step: a 28 dp `surfaceContainer` circle with the number (`labelLarge`), the description (`bodyLarge`, no truncation), and the duration chip below it when present. Steps have 12 dp vertical padding and `hairline` dividers.

---

## 4. Data → UI rules

| Data | UI |
|---|---|
| `imageUrl = null` | `surfaceContainer` placeholder with the `restaurant` icon (`outline` color). Real images load with Coil and show the same placeholder while loading or on error. |
| Offline image | Coil's default disk cache, no extra setup. An image never loaded before shows the same placeholder. |
| `rating = null` | "No ratings" (list); "–" with the label "Rating" (detail). Never show `0.0`. |
| `unit = null` | Quantity only: "2" (for example, "Eggs · 2"). |
| `unit = pinch` or `cup` | "1 pinch", "1 cup" (plural "2 pinches", "2 cups"). The other units are abbreviations and stay the same. |
| Fractional quantity | 0.25 → ¼, 0.5 → ½, 0.75 → ¾, 1.5 → 1½. Otherwise up to 1 decimal place. |
| Servings stepper | Scales every quantity by `selected / original`, with "Scaled from N servings · Reset" (a `TextButton`). Range 1–20. |
| Time (prep, cook, step, totals) | At most 2 units, the largest first, zero units skipped, no space between value and unit: "30s", "1m 30s", "1h", "1h 30m". 3630 s → "1h" (the rest is dropped). |
| Cook time = 0 | Cook stat shows "None". |
| Total time on card | Prep time + cook time. |
| Step with no duration | No duration chip on that step. |
| Difficulty `1` / `2` / `3` | Easy / Medium / Hard. Any other value: hidden on the card, "–" in the Level stat box. |
| `dietaryAttributes = []` | No tags. The recipe is excluded by every diet filter. |
| Long title | 2 lines with an ellipsis in the card; full text on detail. |
| Long description | Detail shows 3 lines + "Read more" (`TextButton`), which expands in place. |
| Search text | Case-insensitive and accent-insensitive (normalize with `java.text.Normalizer` NFD and strip marks), matched against title and description. |
| Ingredient filter | "Contains" match on the normalized ingredient name ("garlic" matches "Garlic cloves, minced"). Included: the recipe must have all of them. Excluded: the recipe must have none of them. |
| Instruction search | "Contains" match on the normalized step descriptions. Some step must match. |
| Diet filter | The recipe must contain **all** selected tags. |
| Difficulty filter | The recipe matches **any** selected difficulty. |
| Pagination | Page-number based, `pageSize = 10`. Stop when `hasNext = false` or a page returns empty. |

---

## 5. Accessibility

- Icon-only buttons need a `contentDescription` ("Filters", "Back", "Add ingredient", "Clear step search", "Fewer servings", "More servings").
- The filter badge is announced with the count ("Filters, 2 active").
- Decorative illustrations and image placeholders use `contentDescription = null`.
- The difficulty bars icon is described ("Difficulty: medium").
- Selected chips and segments expose their selected state (the M3 components do this already).
- Respect font scaling: no fixed heights on text containers, except the M3 component defaults.

---

## 6. Out of scope (do not build)

- Sorting (Recommended / Top rated / Quickest / A–Z).
- "Remove X filter · N recipes" suggestions on the empty search state.
- Skeleton loading for the first load (the spinner is used instead).
- A list ↔ grid toggle.
- A two-pane list/detail layout for tablets and foldables (`ListDetailPaneScaffold`).
