# Recipes

An Android app to browse, search and filter a catalog of recipes, and read one recipe in detail.

## Recipe

**Recipe**:
A dish someone can cook: a title, a description, its ingredients, its instructions and facts like servings and difficulty.
_Avoid_: Dish, item, recipe summary

**Ingredient**:
One line of a recipe's shopping list: a name, a quantity and an optional unit ("Olive oil, 2 tbsp").
_Avoid_: Ingredient line, item

**Instructions**:
The ordered list of steps to cook a recipe.
_Avoid_: Method, directions

**Step**:
One numbered action in the instructions, with an optional duration.
_Avoid_: Instruction

**Dietary tag**:
A fact about which diets a recipe fits, such as vegan or halal. A recipe has zero or more.
_Avoid_: Dietary attribute, diet label

**Difficulty**:
How hard a recipe is: Easy, Medium or Hard. A recipe may have no known difficulty.
_Avoid_: Level

**Rating**:
The average score of a recipe, from 0 to 5. A recipe may have no rating yet.

**Cuisine**:
The cooking tradition a recipe comes from, such as Italian.

## Time

**Prep time**:
How long it takes to prepare the ingredients before cooking.

**Cook time**:
How long the recipe cooks. Zero means it needs no cooking.

**Total time**:
Prep time plus cook time.
_Avoid_: Duration

**Step duration**:
How long one step takes. Not every step has one.

**Instructions time**:
The sum of the known step durations of a recipe.

## Servings

**Servings**:
How many people the recipe, as written, feeds.
_Avoid_: Portions, yield

**Servings range**:
A band of servings the reader filters by: 1–2, 3–4, 5–6 or 7+. It compares against the recipe's servings, never the scaled servings.

**Scaled servings**:
The number of servings the reader chose; every ingredient quantity grows or shrinks to match it.

## Search

**Search**:
Everything the reader asked for at once: the search text plus the filters. An empty search means the whole catalog.
_Avoid_: Query (for the text alone)

**Search text**:
Free text the reader types to find recipes by their words.
_Avoid_: Query, keyword

**Filters**:
The conditions a recipe must meet, besides the search text, to be in the search result.

**Active filter**:
One condition the reader turned on, counted once each: a dietary tag, a difficulty, a servings range, an ingredient or the instruction search.

**Included ingredient**:
A word the reader requires: every included ingredient must appear in the recipe's ingredients.

**Excluded ingredient**:
A word the reader rules out: no excluded ingredient may appear in the recipe's ingredients.

**Instruction search**:
A filter: text that must appear in at least one step of the recipe. It is not part of the search text.
_Avoid_: Step search, method search

**Search result**:
The recipes that match a search so far, and whether more are left to load.
_Avoid_: Page, recipe page
