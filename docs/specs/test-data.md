# Test data

What the 20 recipes in the assets (`recipes_page_1.json` and `recipes_page_2.json`) exercise, and the expected results of searches on them. Use these as the cases for `FakeRecipeSearch.run` and for checking the app by hand. Matching rules are in [domain.md](domain.md#search), the endpoint in [data.md](data.md).

Recipes are named by title. Their ids are UUIDs.

## The assets

- Two pages of 10. The fake server reads both files as one catalog of 20, in order, and pages and filters it itself. Their own `pagination` blocks only describe the full catalog.
- Catalog: page 1 has `hasNext true`, page 2 has `hasNext false`, page 3 returns `data: []`.
- The page 2 recipes match none of the text, include and instructions searches below, so those results stay the same.
- Field names are camelCase. Every time is in seconds (`prepTimeSeconds`, `cookTimeSeconds`, `durationSeconds`).
- `difficulty` is `1`, `2` or `3`. `unit` is `g`, `kg`, `ml`, `tsp`, `tbsp`, `cup`, `pinch` or `null`. `rating` is 0 to 5 or `null`.
- Dietary tags are explicit: a vegan recipe also lists `vegetarian` and `dairy-free`.

## Edge cases

| Case | Recipe | Check |
|---|---|---|
| No image | Grilled Lemon Herb Chicken, Greek Salad | Placeholder, no crash |
| No rating | Creamy Mushroom Risotto, Fluffy Buttermilk Pancakes | "No ratings" on the card, "–" on detail |
| No dietary tags | Beef Tacos with Fresh Salsa | No tags; no diet filter matches it |
| No step durations at all | Beef Tacos with Fresh Salsa, Chicken Burrito | No duration chips; header shows only "4 steps" |
| Some steps with no duration | Classic Margherita Pizza (steps 3, 5), Salmon Teriyaki Bowl (4), Greek Salad (2), Lamb Shoulder (5) | Those steps have no chip |
| Seconds that are not whole minutes | Chickpea Coconut Curry (30 s), Salmon Teriyaki Bowl (45 s), Creamy Mushroom Risotto (90 s) | "30s", "45s", "1m 30s" |
| Long durations | Lamb Shoulder: cook 4 h, a 14400 s step; Avocado Mousse: a 3600 s step | "4h", "1h" |
| Zero cook time | Avocado Mousse, Greek Salad | Cook stat "None" |
| Unicode, accents, emoji | Mousse au Chocolat Végétalien à l'Avocat 🥑🍫, Pão de Queijo | Renders; "pao" finds Pão de Queijo |
| Very long title and description | Lamb Shoulder | 2 lines + ellipsis on the card; full text and "Read more" on detail |
| Fractional quantity (0.5) | Beef Tacos (lime), Salmon Teriyaki Bowl (cucumber), Greek Salad (cucumber, red onion) | "½", and scaling |
| No unit | Pão de Queijo (eggs), Avocado Mousse (avocados), Grilled Lemon Herb Chicken (chicken breasts) | "2", never "2 null" |
| `pinch` | Margherita Pizza, Grilled Lemon Herb Chicken, Avocado Mousse, Lamb Shoulder | "1 pinch", "2 pinches" when scaled |
| `kg` and many servings | Lamb Shoulder (2 kg, 8 servings) | Unit label; servings 7+ |
| One tag vs many | Margherita Pizza (1), Chickpea Coconut Curry and Avocado Mousse (5) | Card shows 2 tags + "+3" |
| Unknown difficulty | none in the assets | Build one in the mapper test |

## Filter counts

| Filter | Recipes | Count |
|---|---|---|
| `vegetarian` | Margherita Pizza, Chickpea Curry, Mushroom Risotto, Avocado Mousse, Greek Salad, Pão de Queijo, Bruschetta, Spaghetti, Penne, Pancakes, Avocado Toast | 11 |
| `vegan` | Chickpea Curry, Avocado Mousse, Bruschetta, Penne | 4 |
| `pescatarian` | Salmon Teriyaki Bowl, Thai Shrimp Soup | 2 |
| `halal` | Lemon Herb Chicken, Lamb Shoulder, Chicken Skewers, Chicken Burrito, Butter Chicken | 5 |
| `gluten-free` | Chickpea Curry, Lemon Herb Chicken, Mushroom Risotto, Avocado Mousse, Greek Salad, Pão de Queijo, Chicken Skewers, Thai Shrimp Soup, Steak | 9 |
| `dairy-free` | Chickpea Curry, Lemon Herb Chicken, Salmon Bowl, Avocado Mousse, Lamb Shoulder, Bruschetta, Penne, Chicken Skewers, Thai Shrimp Soup, Avocado Toast | 10 |
| `nut-free` | all but Margherita Pizza and Beef Tacos | 18 |
| `low-carb` | Lemon Herb Chicken, Greek Salad, Steak | 3 |
| Easy (`1`) | Chickpea Curry, Lemon Herb Chicken, Beef Tacos, Avocado Mousse, Greek Salad, Bruschetta, Pancakes, Chicken Burrito, Avocado Toast | 9 |
| Medium (`2`) | Margherita Pizza, Salmon Bowl, Pão de Queijo, Spaghetti, Penne, Chicken Skewers, Thai Shrimp Soup | 7 |
| Hard (`3`) | Mushroom Risotto, Lamb Shoulder, Butter Chicken, Steak | 4 |
| Servings 1–2 | Salmon Bowl, Greek Salad, Spaghetti, Pancakes, Chicken Burrito, Avocado Toast, Steak | 7 |
| Servings 3–4 | Margherita Pizza, Chickpea Curry, Lemon Herb Chicken, Mushroom Risotto, Beef Tacos, Avocado Mousse, Penne, Chicken Skewers, Thai Shrimp Soup | 9 |
| Servings 5–6 | Pão de Queijo, Bruschetta, Butter Chicken | 3 |
| Servings 7+ | Lamb Shoulder | 1 |

## Searches

| Search | Result |
|---|---|
| Text "pao" | Pão de Queijo |
| Text "vegetalien" | Avocado Mousse (title) |
| Text "creme" | Avocado Mousse (description: "Crème de la crème") |
| Diet `vegan` + `gluten-free` | Chickpea Curry, Avocado Mousse (must have all) |
| Diet `vegan` + `halal` | nothing: the empty search state |
| Diet `vegetarian` + servings 3–4 + Easy | Chickpea Curry, Avocado Mousse |
| Include "garlic" | Chickpea Curry, Lemon Herb Chicken ("Garlic cloves, minced") |
| Exclude "garlic" | the other 18 |
| Include "olive oil", exclude "garlic" | Margherita Pizza, Greek Salad, Lamb Shoulder |
| Exclude "cheese" | the 18 without "cheese" in an ingredient name. Beef Tacos stays: "Grated cheddar" doesn't contain "cheese" |
| Instructions "oven" | Margherita Pizza, Pão de Queijo |
| Instructions "saute" | Mushroom Risotto ("Sauté") |
| Instructions "oven", exclude "cheese" | Margherita Pizza |
| A page past the last one | `data: []`, `hasNext: false` |
